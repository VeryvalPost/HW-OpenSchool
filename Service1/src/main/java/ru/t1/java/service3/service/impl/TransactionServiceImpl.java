package ru.t1.java.service3.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.service3.dto.TransactionDTO;
import ru.t1.java.service3.kafka.KafkaTransactionalProducer;
import ru.t1.java.service3.model.*;
import ru.t1.java.service3.repository.AccountRepository;
import ru.t1.java.service3.repository.TransactionRepository;
import ru.t1.java.service3.service.TransactionService;
import ru.t1.java.service3.service.UniqueIdGeneratorService;
import ru.t1.java.service3.util.TransactionMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("${spring.kafka.topic.transactions}")
    private String topicTransactions;
    @Value("${spring.kafka.topic.transactionsAccept}")
    private String topicTransactionsAccept;

    @Value("${spring.kafka.topic.transactionsResult}")
    private String topicTransactionsResult;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final KafkaTransactionalProducer<TransactionDTO> kafkaTransactionalProducer;
    private final UniqueIdGeneratorService idGenerator;


    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  KafkaTransactionalProducer<TransactionDTO> kafkaTransactionalProducer,
                                  UniqueIdGeneratorService idGenerator
                                  ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.kafkaTransactionalProducer = kafkaTransactionalProducer;
        this.idGenerator = idGenerator;
    }


    //   @PostConstruct
    void init() {
        try {
            List<Transaction> transactions = parseJson();
            transactionRepository.saveAll(transactions);
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
    }




    @Override
    public List<Transaction> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        TransactionDTO[] transactions = mapper.readValue(new File("src/main/resources/TRANSACTION_DATA.json"), TransactionDTO[].class);

        return Arrays.stream(transactions)
                .map(dto -> {
                    String account_id = dto.getGlobalAccountId();
                    Account account = accountRepository.findAccountByGlobalAccountId(account_id).orElseThrow(() -> new IllegalArgumentException("Не найден аккаунт: " + account_id));
                    return TransactionMapper.toEntity(dto, account);
                })
                .collect(Collectors.toList());
    }

    // Сделано для тестирования producer и consumer Kafka
    @Override
    public void sendTransactionToKafka(String topic, Transaction transaction) {
        TransactionDTO transactionDTO = TransactionMapper.toDto(transaction);
        kafkaTransactionalProducer.sendTo(topic, transactionDTO);
    }



    @Transactional
    public Transaction createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setGlobalTransactionId(idGenerator.generateId(EntityType.TRANSACTION));
        transactionRepository.save(transaction);
        return transaction;
    }

    // Метод относится к 4ому заданию и блокирует транзацкии
    @Transactional
    public String blockTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.REJECTED);
        transactionRepository.save(transaction);
        TransactionDTO transactionDTO = TransactionMapper.toDto(transaction);
        kafkaTransactionalProducer.sendTo(topicTransactionsResult, transactionDTO);
        log.info("Транзакция блокирована. Сообщение для транзакции ID {} отправлено в топик {}", transaction.getGlobalTransactionId(), topicTransactionsResult);
        return TransactionStatus.REJECTED.name();
    }
}
