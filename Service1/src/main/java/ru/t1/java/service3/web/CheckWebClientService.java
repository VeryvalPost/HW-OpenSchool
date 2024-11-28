package ru.t1.java.service3.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.service3.dto.CheckRequest;
import ru.t1.java.service3.dto.CheckResponse;
import ru.t1.java.service3.exception.ExternalServiceException;

import java.util.Objects;

@Slf4j
@Component
public class CheckWebClientService {
    @Value("${external.check_service.url}")
    private String externalServiceUrl;

    private final RestTemplate restTemplate;


    public CheckWebClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isClientBlacklisted(String globalClientId, String globalAccountId) {
        String url = externalServiceUrl + "/api/checkClient";

        // Получение JWT токена
        String jwtToken = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials()
                .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        CheckRequest checkRequest = CheckRequest.builder()
                .globalClientId(globalClientId)
                .globalAccountId(globalAccountId)
                .build();

        HttpEntity<CheckRequest> requestEntity = new HttpEntity<>(checkRequest, headers);

        try {
            ResponseEntity<CheckResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    CheckResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return Objects.requireNonNull(response.getBody()).isBlocked();
            } else {
                log.error("Ошибка при проверке клиента. Статус: {}, Тело ответа: {}",
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