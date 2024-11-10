package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.AccountService;



import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final ClientRepository clientRepository;

    @Autowired
    public AccountController(AccountService accountService, ClientRepository clientRepository) {
        this.accountService = accountService;
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account, @RequestParam Long clientId) {
        log.info("Создание нового аккаунта для клиента с ID: {}", clientId);
        Account createdAccount = accountService.createAccount(account, clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account updatedAccount) {
        log.info("Изменение аккаунта для клиента с ID: {}", id);
        Account account = accountService.updateAccount(id, updatedAccount);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {
        log.info("Закрытие аккаунта  с ID: {}", id);
        accountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> allTransaction(@PathVariable Long id) {
        log.info("Перечень всех транзакций для аккаунта ID: {}", id);
        List<Transaction> transactions = accountService.findAllAccountTransactions(id);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/testKafka")
    public ResponseEntity<String> sendAccountToKafka() {
        log.info("Тестовая отсылка записи в топик аккаунтов ID: {}");
        accountService.sendAccountToKafka();
        return ResponseEntity.status(200).body("Сообщение успешно отправлено в Kafka");
    }
}