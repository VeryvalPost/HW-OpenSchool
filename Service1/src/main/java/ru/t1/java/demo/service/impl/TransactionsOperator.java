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
import ru.t1.java.demo.service.ClientService;
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

    @Value("${transaction.rejection}")
    private long rejectedTransactionLimit;

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
    @Autowired
    private final  ClientService clientService;


    public TransactionsOperator(TransactionService transactionService,
                                TransactionRepository transactionRepository,
                                AccountRepository accountRepository,
                                CheckWebClientService webClientService,
                                AccountService accountService,
                                ClientService clientService) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.webClientService = webClientService;
        this.accountService = accountService;
        this.clientService = clientService;
    }

    @Override
    @Transactional
    public void operate(String topic, Transaction transaction) {
        log.info("Обработка транзакции для топика: {}", topic);
    /*
     К заданию с обработкой транзакций и проверкой статусов BLOCKED у клиента.
     */
        Boolean clientStatus = transaction.getAccount().getClient().getStatus();
        log.debug("Статус клиента: {} для транзакции ID: {}", clientStatus, transaction.getGlobalTransactionId());

        if (clientStatus == null) {
            log.info("Статус клиента не определен, выполняется проверка на блокировку, транзакция ID: {}", transaction.getGlobalTransactionId());

            if (checkBlockedClientAtNewTransaction(transaction)) {
                log.warn("Клиент заблокирован, инициируем блокировку клиента, счета и транзакции, транзакция ID: {}", transaction.getGlobalTransactionId());

                clientService.blockClient(transaction.getAccount().getClient().getGlobalId());
                log.info("Клиент с ID {} успешно заблокирован", transaction.getAccount().getClient().getGlobalId());

                accountService.blockAccount(transaction.getGlobalAccountId());
                log.info("Счет с ID {} успешно заблокирован", transaction.getGlobalAccountId());

                transactionService.blockTransaction(transaction);
                log.info("Транзакция с ID {} успешно заблокирована", transaction.getGlobalTransactionId());
                return;
            }
        } else if (transaction.getStatus().equals(TransactionStatus.REJECTED)) {
            log.info("Обработка транзакции с отклоненным статусом (REJECTED), транзакция ID: {}", transaction.getGlobalTransactionId());

            List<Transaction> transactionList = transactionRepository.findAllLastTransactions(transaction.getGlobalAccountId());
            log.debug("Найдено {} последних транзакций для счета ID {}", transactionList.size(), transaction.getGlobalAccountId());

            long allRejectedTransactions = transactionList.stream()
                    .filter(t -> t.getStatus().equals(TransactionStatus.REJECTED))
                    .count();
            log.debug("Количество отклоненных транзакций: {} для счета ID {}", allRejectedTransactions, transaction.getGlobalAccountId());

            if (allRejectedTransactions > rejectedTransactionLimit) {
                log.warn("Превышен лимит отклоненных транзакций для счета ID {}, блокируем счет", transaction.getGlobalAccountId());
                accountService.changeAccountStatus(transaction.getGlobalAccountId(), AccountStatus.ARRESTED);
                log.info("Счет с ID {} переведен в статус ARRESTED", transaction.getGlobalAccountId());
            }
        }
    /*
     Если по условиям 4ог задания все отлично, то продолжаем как ранее.
     */
        if (topic.equals(topicTransactions)) {
            log.info("Обработка транзакции по топику: {}, транзакция ID: {}", topic, transaction.getGlobalTransactionId());
            operateTransactionMessage(transaction);
        } else if (topic.equals(topicTransactionsResult)) {
            log.info("Обработка результата транзакции из топика: {}, транзакция ID: {}", topic, transaction.getGlobalTransactionId());
            operateTransactionResult(transaction);
        } else {
            log.warn("Неизвестный топик: {}, транзакция ID {}", topic, transaction.getGlobalTransactionId());
        }
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
                        .toList();
                double frozenAmount = 0.0;
                for (Transaction trans : filteredTransactions) {
                    trans.setStatus(TransactionStatus.BLOCKED);
                    accountService.changeBalance(trans);
                    frozenAmount += trans.getAmount();
                    transactionRepository.save(trans);
                    log.info("Транзакция с ID {} обновлена на BLOCKED и баланс скорректирован", trans.getGlobalTransactionId());
                }

                log.info("Найдено {} транзакций со статусом REQUESTED для блокировки", filteredTransactions.size());
                blockedAccount.setFrozenAmount(frozenAmount);
                accountRepository.save(blockedAccount);
                log.info("Замороженная сумма на счете ID {} установлена в {}", blockedAccount.getGlobalAccountId(), frozenAmount);
            }

        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных: {}", e.getMessage());
            throw new AccountException("Не удалось выполнить операцию транзакции, ошибка БД:", e);
        }

        return transaction.getStatus().name();
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
        String globalAccountId = transaction.getAccount().getGlobalAccountId();
        try {
            return webClientService.isClientBlacklisted(globalClientId, globalAccountId);
        } catch (ExternalServiceException e) {
            log.error("Ошибка при проверке клиента {} с аккаунтом {}: {}", globalClientId, globalAccountId, e.getMessage(), e);
            return false;
        }
    }

}
