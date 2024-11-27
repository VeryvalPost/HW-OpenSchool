package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.dto.AccountDTO;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.exception.TransactionException;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.UniqueIdGeneratorService;
import ru.t1.java.demo.util.AccountMapper;
import t1.demo.starter.aop.LogDataSourceError;
import t1.demo.starter.aop.Metrics;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Value("${spring.kafka.topic.accounts}")
    private String topic;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    private final KafkaAccountProducer<AccountDTO> kafkaAccountProducer;
    private final UniqueIdGeneratorService idGenerator;

    private final TransactionRepository transactionRepository;
    public AccountServiceImpl(AccountRepository accountRepository,
                              ClientRepository clientRepository,
                              TransactionRepository transactionRepository,
                              KafkaAccountProducer<AccountDTO> kafkaAccountProducer,
                                UniqueIdGeneratorService idGenerator) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaAccountProducer =kafkaAccountProducer;
        this.idGenerator = idGenerator;
    }


   // @PostConstruct
    void init() {
        try {
            List<Account> accounts = parseJson();
            accountRepository.saveAll(accounts);
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
    }
    @Override
    public Account createAccount(Account account, String globalClientId) {
        try {
        Optional<Client> clientOpt = clientRepository.findClientByGlobalId(globalClientId);
        if (clientOpt.isPresent()) {
            Client currentClient = clientOpt.get();
            account.setClient(currentClient);
            account.setGlobalAccountId(idGenerator.generateId(EntityType.ACCOUNT));

            return accountRepository.save(account);
        } else {
            throw new ClientException("Клиент с ID " + globalClientId + " не найден");
        }
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных при создании аккаунта для клиента с ID: {}", globalClientId, e);
            throw new AccountException("Не получилось создать аккаунт пользователя, ошибка БД:", e);
        }
    }

    @Override
    public Account updateAccount(String globalAccountId, Account updatedAccount) {
        try {
            Optional<Account> existingAccountOpt = accountRepository.findAccountByGlobalAccountId(globalAccountId);
            if (existingAccountOpt.isPresent()) {
                Account existingAccount = existingAccountOpt.get();
                existingAccount.setType(updatedAccount.getType());
                existingAccount.setBalance(updatedAccount.getBalance());
                return accountRepository.save(existingAccount);
            } else {
                throw new AccountException("Аккаунт не найден ID" + globalAccountId);
            }
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных для : {}", globalAccountId, e);
            throw new AccountException("Не получилось обновить аккаунт пользователя, ошибка БД:", e);
        }
    }

    @Override
    public void changeAccountStatus(String globalAccountId,AccountStatus status) {
        try {
            Optional<Account> existingAccountOpt = accountRepository.findAccountByGlobalAccountId(globalAccountId);
            if (existingAccountOpt.isPresent()) {
                Account existingAccount = existingAccountOpt.get();
                existingAccount.setStatus(status);
                accountRepository.save(existingAccount);
            } else {
                throw new AccountException("Аккаунт не найден ID" + globalAccountId);
            }
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных для : {}", globalAccountId, e);
            throw new AccountException("Не получается закрыть счет, ошибка БД:", e);
        }
    }

   @Override
   public List<Transaction> findAllAccountTransactions(String globalAccountId) {
        try {
            List<Transaction> transactions = transactionRepository.findAllTransactionByGlobalAccountId(globalAccountId);
            if (transactions == null || transactions.isEmpty()) {
                log.warn("Не найдено транзакций для аккаунта с ID: {}", globalAccountId);
                return Collections.emptyList();
            }
            return transactions;
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных для : {}", globalAccountId, e);
            throw new TransactionException("Не получилось выполнить транзакцию.", e);
        }
    }

    @Override
    public List<Account> parseJson() throws IOException {

            ObjectMapper mapper = new ObjectMapper();

            AccountDTO[] accounts = mapper.readValue(new File("src/main/resources/ACCOUNT_DATA.json"), AccountDTO[].class);

        return Arrays.stream(accounts)
                .map(dto -> {
                    Long clientId = dto.getId();
                    Client client = clientRepository.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Не найден клиент: " + clientId));
                    return AccountMapper.toEntity(dto, client);
                })
                .collect(Collectors.toList());
        }


// Сделано для тестирования producer и consumer Kafka
    public void sendAccountToKafka() {
        // Пример отправки в Kafka
     //   AccountDTO accountDTO = new AccountDTO(1710L, "OPEN","DEBIT", 156.0, 2L);
       // kafkaAccountProducer.sendTo(topic, accountDTO);
        }

        /*
        К заданию №4
                 */
    @Override
    public void blockAccount(String globalAccountId) {
        Optional<Account> optAccount = accountRepository.findAccountByGlobalAccountId(globalAccountId);
        if (optAccount.isPresent()){
            Account account = optAccount.get();
            account.setStatus(AccountStatus.BLOCKED);
            accountRepository.save(account);
            log.info("Блокирую аккаунт(счет): {}", globalAccountId);
        }
    }


    @Transactional
    @Metrics
    @LogDataSourceError
    public void changeBalance(Transaction transaction) {
        try {
            Optional<Account> curAccount = accountRepository.findAccountByGlobalAccountId(transaction.getAccount().getGlobalAccountId());
            log.info("Обрабатываем транзакцию, ID: {}, сумма: {}, счет: {}",
                    transaction.getGlobalTransactionId(),
                    transaction.getAmount(),
                    transaction.getAccount().getId());

            if (curAccount.isPresent()) {
                Double balance = curAccount.get().getBalance();
                Double amount = transaction.getAmount();
                Account account = curAccount.get();
                account.setBalance(balance + amount);
                accountRepository.save(account);
                log.info("Изменен баланс" + transaction.getAccount().getGlobalAccountId() + " на сумму" + transaction.getAmount());
            }
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных для : {}", e.getMessage());
            throw new AccountException("Не получилось выполнить операцию транзакции, ошибка БД:", e);
        }
    }




}





