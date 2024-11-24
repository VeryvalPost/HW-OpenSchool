package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountStatus;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account, @RequestParam("globalClientId") String globalClientId) {
        log.info("Создание нового аккаунта для клиента с ID: {}", globalClientId);
        Account createdAccount = accountService.createAccount(account, globalClientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable String globalAccountId, @RequestBody Account updatedAccount) {
        log.info("Изменение аккаунта для клиента с ID: {}", globalAccountId);
        Account account = accountService.updateAccount(globalAccountId, updatedAccount);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{globalAccountId}/changeStatus")
    public ResponseEntity<Void> chanceStatus(@PathVariable("globalAccountId") String globalAccountId, @RequestParam("status") AccountStatus status) {
        log.info("Смена статуса аккаунта  с ID: {}", globalAccountId);
        accountService.changeAccountStatus(globalAccountId, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> allTransaction(@PathVariable String id) {
        log.info("Перечень всех транзакций для аккаунта ID: {}", id);
        List<Transaction> transactions = accountService.findAllAccountTransactions(id);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/testKafka")
    public ResponseEntity<String> sendAccountToKafka() {
        log.info("Тестовая отсылка записи в Kafka для аккаунта");
        try {
            accountService.sendAccountToKafka();
            return ResponseEntity.status(200).body("Сообщение успешно отправлено в Kafka");
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в Kafka", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при отправке сообщения в Kafka");
        }
    }

    @PostMapping("/{id}/changeBalance")
    public ResponseEntity<String> changeBalance(@RequestBody Transaction transaction) {
        try {
            log.info("Выполнение изменений баланса по транзакции {}", transaction.getGlobalTransactionId());
            accountService.changeBalance(transaction);
            log.info("Баланс счета {} изменен на сумму {} ", transaction.getAccount().getGlobalAccountId(), transaction.getAmount());
            return ResponseEntity.status(HttpStatus.OK).body(transaction.getGlobalTransactionId());
        } catch (Exception e) {
            log.error("Не удалось изменить баланс {}", transaction.getAccount().getGlobalAccountId(),  e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Не удалось изменить баланс");
        }
    }

}