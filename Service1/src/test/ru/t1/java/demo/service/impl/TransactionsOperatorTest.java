package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import ru.t1.java.demo.dto.TransactionDTO;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.util.TransactionMapper;
import ru.t1.java.demo.web.CheckWebClientService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class TransactionsOperatorTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private ClientService clientService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CheckWebClientService webClientService;

    @InjectMocks
    private TransactionsOperator transactionsOperator;
    private Transaction transaction;
    private Account account;

    @BeforeEach
    void setUp() {

        account = new Account();
        account.setId(1L);
        account.setGlobalAccountId("ACT-00000001");
        account.setStatus(AccountStatus.OPEN);

        Client client = new Client();;
        client.setGlobalId("CLT-00000001");
        client.setStatus(true);

        account.setClient(client);
        account.setBalance(200.0);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setGlobalTransactionId("TRX-00000001");
        transaction.setAccount(account);
        transaction.setStatus(TransactionStatus.REQUESTED);
    }
        @Test
        void testOperateTransactionResult_accepted() {
            transaction.setStatus(TransactionStatus.ACCEPTED);

            when(transactionRepository.save(transaction)).thenReturn(transaction);

            String result = transactionsOperator.operateTransactionResult(transaction);

            verify(transactionRepository).save(transaction);
            assertEquals(TransactionStatus.ACCEPTED.name(), result);
        }

    @Test
    void testOperateTransactionResult_blocked() {
        transaction.setStatus(TransactionStatus.BLOCKED);

        Transaction transactionRequested = new Transaction();
        transactionRequested.setId(2L);
        transactionRequested.setGlobalTransactionId("TRX-00000002");
        transactionRequested.setAccount(account);
        transactionRequested.setStatus(TransactionStatus.REQUESTED);
        transactionRequested.setAmount(300.0);
        transactionRepository.save(transactionRequested);

        when(transactionRepository.findAllTransactionByGlobalAccountId(account.getGlobalAccountId()))
                .thenReturn(List.of(transactionRequested));

        when(accountRepository.save(account)).thenReturn(account);
        when(transactionRepository.save(transactionRequested)).thenReturn(transactionRequested);

        String result = transactionsOperator.operateTransactionResult(transaction);

        assertEquals(TransactionStatus.BLOCKED.name(), result);
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
        assertEquals(300.0, account.getFrozenAmount());
    }


    @Test
    void testOperateTransactionMessage_Success() {

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        String result = transactionsOperator.operateTransactionMessage(transaction);

        assertEquals("TRX-00000001", result);
        verify(transactionRepository).save(transaction);
     }

    @Test
    void testOperateTransactionMessage_Exception() {
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new DataAccessException("Ошибка БД") {});


        AccountException exception = assertThrows(AccountException.class,
                () -> transactionsOperator.operateTransactionMessage(transaction));

        assertTrue(exception.getMessage().contains("Не удалось выполнить операцию транзакции"));
        verify(transactionRepository).save(transaction);
    }



}