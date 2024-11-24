package ru.t1.java.service2.controller;


import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.service2.dto.CheckRequest;
import ru.t1.java.service2.dto.CheckResponse;
import ru.t1.java.service2.service.impl.ClientAcceptService;
import ru.t1.java.service2.util.JwtUtils;

@RestController
@Slf4j
public class BlackListController {

        @Autowired
        private final ClientAcceptService clientAcceptService;


    public BlackListController(ClientAcceptService clientAcceptService
                               ) {
        this.clientAcceptService = clientAcceptService;
    }

    @PostMapping("/api/checkClient")
    public ResponseEntity<CheckResponse> check(@RequestBody CheckRequest checkRequest) {

        log.info("Проверка клиента с ID: {}", checkRequest.getGlobalClientId());

        boolean isBlocked = clientAcceptService.checkClientBlocked(checkRequest.getGlobalClientId());

        log.debug("Результат проверки: {}", isBlocked);
        CheckResponse response = CheckResponse.builder()
                .blocked(isBlocked)
                .build();

        return ResponseEntity.ok(response);
    }



}
