package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.web.CheckWebClientService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    private final CheckWebClientService webClientService;

    @LogException
    @Track
    @GetMapping(value = "/client")
    @HandlingResult
    public void doSomething() throws IOException, InterruptedException {
//        try {
//            clientService.parseJson();
        Thread.sleep(3000L);
        throw new ClientException();
//        } catch (Exception e) {
//            log.info("Catching exception from ClientController");
//            throw new ClientException();
//        }
    }


    @PostMapping("/registerClient")
    public ResponseEntity<Client> createAccount(@RequestBody Client client) {
        log.info("Создание нового клиента с ID: {}", client);
        Client createdClient = clientService.registerClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @PostMapping("/checkClient")
    public ResponseEntity<String> checkClient(@RequestParam("globalClientId") String globalClientId,
                                              @RequestParam("globalAccountId") String globalAccountId) {
        log.info("Проверка клиента с globalID: {}", globalClientId);
        try {
            boolean isBlocked = webClientService.isClientBlacklisted(globalClientId, globalAccountId);

            if (isBlocked) {
                return ResponseEntity.ok("{\"blocked\": true}");
            } else {
                return ResponseEntity.ok("{\"blocked\": false}");
            }
        } catch (Exception e) {
            // Обработка ошибок, если что-то пошло не так
            log.error("Ошибка при проверке клиента: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Ошибка при обработке запроса\"}");
        }


    }
}