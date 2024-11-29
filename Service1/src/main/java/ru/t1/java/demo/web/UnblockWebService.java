package ru.t1.java.demo.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.exception.ExternalServiceException;
import ru.t1.java.demo.dto.UnblockRequest;
import ru.t1.java.demo.dto.UnblockResponse;
import ru.t1.java.demo.model.Role;
import ru.t1.java.demo.model.RoleEnum;
import ru.t1.java.demo.model.User;
import ru.t1.java.demo.repository.UserRepository;
import ru.t1.java.demo.service.impl.AuthService;
import ru.t1.java.demo.service.impl.UserDetailsImpl;
import ru.t1.java.demo.util.JwtUtils;

import java.util.*;

import java.util.stream.Collectors;

@Slf4j
@Component
public class UnblockWebService {
    @Value("${external.unblock_service.url}")
    private String externalServiceUrl;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final AuthService authService;

    public UnblockWebService(RestTemplate restTemplate,
                             UserRepository userRepository,
                             AuthService authService) {
        this.restTemplate = restTemplate;
        this.userRepository =  userRepository;
        this.authService = authService;
    }

    public List<String> unblockList(List<String> globalIdList) {
        String url = externalServiceUrl + "/api/unblock";

        Optional<User> serviceUserOpt = userRepository.findByLogin("service");

        if (serviceUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Пользователь 'service' не найден в базе данных");
        }

        User serviceUser = serviceUserOpt.get();
        serviceUser.setPassword("service"); // плохое решение. Но для быстроты и для проверки функционала
        String jwtToken = authService.authentication(serviceUser);


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UnblockRequest unblockRequest = UnblockRequest.builder()
                .globalIdList(globalIdList)
                .build();

        HttpEntity<UnblockRequest> requestEntity = new HttpEntity<>(unblockRequest, headers);

        try {
            ResponseEntity<UnblockResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    UnblockResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return Objects.requireNonNull(response.getBody()).getUnblockedList();
            } else {
                log.error("Ошибка сервиса разблокировки. Статус: {}, Тело ответа: {}",
                        response.getStatusCode(),
                        response.getBody());

                throw new ExternalServiceException("Неожиданный статус ответа: " + response.getStatusCode());
            }

        } catch (HttpStatusCodeException e) {
            log.error("Ошибка при запросе к внешнему сервису. Код: {}, Тело ответа: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(), e);

            throw new ExternalServiceException("Ошибка при проверке клиента. Код: " + e.getStatusCode(), e);

        } catch (ResourceAccessException e) {
            log.error("Сетевая ошибка при обращении к внешнему сервису", e);
            throw new ExternalServiceException("Сетевая ошибка при обращении к внешнему сервису", e);

        } catch (Exception e) {
            log.error("Непредвиденная ошибка при проверке клиента", e);
            throw new ExternalServiceException("Непредвиденная ошибка при проверке клиента", e);
        }
    }
}