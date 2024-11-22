package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.dto.CheckRequest;
import ru.t1.java.demo.dto.CheckResponse;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

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



}
