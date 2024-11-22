package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.kafka.KafkaTransactionalProducer;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.UniqueIdGeneratorService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KafkaTransactionalProducer<TransactionDTO> kafkaTransactionalProducer;

    @Mock
    private UniqueIdGeneratorService idGenerator;

    @InjectMocks
    private TransactionServiceImpl transactionService;
    @Test
    void operateTransactionResult() {
    }

    @Test
    void operateTransactionMessage() {
        Client mockClient = new Client("CLT-000001","John", "Doe", "Middle", true, null);
        Account mockAccount = new Account(1L, "ACC-000001", AccountType.CREDIT,AccountStatus.OPEN, 100.0, 0.0, null,null );
        mockAccount.setClient(mockClient);
        mockClient.setAccounts(Set.of(mockAccount));
        accountRepository.save(mockAccount);

        Transaction transaction = new Transaction();
        transaction.setGlobalTransactionId("TRX-00000001");
        transaction.setAccount(mockAccount);
        transaction.setAmount(50.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);


        String result = transactionService.operateTransactionMessage(transaction);


        assertEquals("TRX-00000001", result);
        verify(transactionRepository).save(transaction);
        verify(kafkaTransactionalProducer).sendTo(eq(null), any(TransactionDTO.class));

    }

    @Test
    void createTransaction() {
    }
}