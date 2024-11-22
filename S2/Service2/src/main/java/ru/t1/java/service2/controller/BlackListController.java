package ru.t1.java.service2.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.service2.dto.CheckRequest;
import ru.t1.java.service2.dto.CheckResponse;
import ru.t1.java.service2.service.impl.ClientService;
import ru.t1.java.service2.util.JwtUtils;

@RestController
@Slf4j
public class BlackListController {

        @Autowired
        private final ClientService clientService;
    private final JwtUtils jwtUtils;

    public BlackListController(ClientService clientService, JwtUtils jwtUtils) {
        this.clientService = clientService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/api/checkClient")
    public ResponseEntity<ru.t1.java.service2.dto.CheckResponse> check(@RequestHeader("Authorization") String authorizationHeader,
                                                                       @RequestBody CheckRequest checkRequest) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);

        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isBlocked = clientService.checkClientBlocked(checkRequest.getGlobalClientId());
        CheckResponse response = CheckResponse.builder()
                .blocked(isBlocked)
                .build();
        return ResponseEntity.ok(response);
    }



}
