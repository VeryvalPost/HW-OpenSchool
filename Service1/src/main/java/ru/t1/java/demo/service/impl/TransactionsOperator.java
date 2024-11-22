package ru.t1.java.demo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.exception.ExternalServiceException;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionOperator;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.web.CheckWebClientService;

import java.util.List;

@Service
@Slf4j
public class TransactionsOperator implements TransactionOperator {
    @Value("${spring.kafka.topic.transactions}")
    private String topicTransactions;
    @Value("${spring.kafka.topic.transactionsAccept}")
    private String topicTransactionsAccept;

    @Value("${spring.kafka.topic.transactionsResult}")
    private String topicTransactionsResult;
    @Autowired
    private final TransactionService transactionService;
    @Autowired
    private final TransactionRepository transactionRepository;

    @Autowired
    private final AccountRepository accountRepository;
    @Autowired
    private final CheckWebClientService webClientService;

    @Autowired
    private final AccountService accountService;


    public TransactionsOperator(TransactionService transactionService,
                                TransactionRepository transactionRepository,
                                AccountRepository accountRepository,
                                CheckWebClientService webClientService,
                                AccountService accountService) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.webClientService = webClientService;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public void operate(String topic, Transaction transaction) {
        log.info("Обработка транзакции для топика: {}", topic);
    /*
     К заданию с обработкой транзакций и проверкой статусов BLOCKED у клиента.
     */

        checkBlockedClientAtNewTransaction(transaction);


        if (topic.equals(topicTransactions)) {
            operateTransactionMessage(transaction);
        } else if (topic.equals(topicTransactionsResult)) {
            operateTransactionResult(transaction);
        }
        log.warn("Неизвестный топик: {}, транзакция ID {}", topic, transaction.getGlobalTransactionId());
    }

    /*
     К заданию с обработкой транзакций из топиков Result и Accept
     */
    @Transactional
    public String operateTransactionResult(Transaction transaction) {
        // Обработка сообщений из топика t1_demo_transaction_result
        try {
            log.info("Получено сообщение из топика t1_demo_transaction_result для транзакции с ID {}", transaction.getGlobalTransactionId());

            if (transaction.getStatus() == TransactionStatus.ACCEPTED) {
                transactionRepository.save(transaction);
                log.info("Транзакция с ID {} обновлена со статусом ACCEPTED", transaction.getGlobalTransactionId());
            }

            if (transaction.getStatus() == TransactionStatus.BLOCKED) {
                Account blockedAccount = transaction.getAccount();
                blockedAccount.setStatus(AccountStatus.BLOCKED);
                log.info("Статус счета для ID {} обновлен на BLOCKED", blockedAccount.getGlobalAccountId());


                List<Transaction> transactions = transactionRepository.findAllTransactionByGlobalAccountId(blockedAccount.getGlobalAccountId());
                List<Transaction> filteredTransactions = transactions.stream()
                        .filter(t -> t.getStatus() == TransactionStatus.REQUESTED)
                        .peek(t -> t.setStatus(TransactionStatus.BLOCKED))
                        .toList();
                log.info("Найдено {} транзакций со статусом REQUESTED для блокировки", filteredTransactions.size());


                double frozenAmount = 0.0;
                for (Transaction trans : filteredTransactions) {
                    accountService.changeBalance(trans);
                    frozenAmount += trans.getAmount();
                    transactionRepository.save(trans);
                    log.info("Транзакция с ID {} обновлена на BLOCKED и баланс скорректирован", trans.getGlobalTransactionId());
                }


                blockedAccount.setFrozenAmount(frozenAmount);
                accountRepository.save(blockedAccount);
                log.info("Замороженная сумма на счете ID {} установлена в {}", blockedAccount.getGlobalAccountId(), frozenAmount);
            }

        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных: {}", e.getMessage());
            throw new AccountException("Не удалось выполнить операцию транзакции, ошибка БД:", e);
        }

        return TransactionStatus.CANCELLED.name();
    }

    @Transactional
    public String operateTransactionMessage(Transaction transaction) {
        // Обработка сообщений из топика t1_demo_transactions

        try {
            log.info("Получено сообщение из топика t1_demo_transactions для транзакции с ID {}", transaction.getGlobalTransactionId());

            if (transaction.getAccount().getStatus() == AccountStatus.OPEN) {
                log.info("Счет открыт, сохраняем транзакцию и обновляем баланс");


                transaction.setStatus(TransactionStatus.REQUESTED);
                transactionRepository.save(transaction);
                log.info("Транзакция с ID {} сохранена со статусом REQUESTED", transaction.getGlobalTransactionId());

                accountService.changeBalance(transaction);
                log.info("Баланс счета обновлен для транзакции с ID {}", transaction.getGlobalTransactionId());

                transactionService.sendTransactionToKafka(topicTransactionsAccept, transaction);
                log.info("Сообщение отправлено в Kafka для транзакции с ID {}", transaction.getGlobalTransactionId());
            }

            return transaction.getGlobalTransactionId();

        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных: {}", e.getMessage());
            throw new AccountException("Не удалось выполнить операцию транзакции, ошибка БД:", e);
        }

    }


    /*
     К заданию с обработкой транзакций и проверкой статусов BLOCKED у клиента.
     */

    public boolean checkBlockedClientAtNewTransaction(Transaction transaction) {
        String globalClientId = transaction.getAccount().getClient().getGlobalId();
        try {
            return webClientService.isClientBlacklisted(globalClientId);
        } catch (ExternalServiceException e) {
            log.error("Ошибка при проверке клиента {}: {}", globalClientId, e.getMessage(), e);
            return false;
        }
    }

}
