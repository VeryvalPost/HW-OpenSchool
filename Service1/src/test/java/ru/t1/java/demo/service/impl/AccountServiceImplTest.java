package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.dto.AccountDTO;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.UniqueIdGeneratorService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private KafkaAccountProducer<AccountDTO> kafkaAccountProducer;

    @Mock
    private UniqueIdGeneratorService idGenerator;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAccount() {

        Client mockClient = new Client("CLT-000001","John", "Doe", "Middle", true, null);
        Account mockAccount = new Account(1L, "ACC-000001", AccountType.CREDIT,AccountStatus.OPEN, 1000.0, 0.0, null,null );
        mockAccount.setClient(mockClient);
        mockClient.setAccounts(Set.of(mockAccount));

        Mockito.when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));
        Mockito.when(idGenerator.generateId(EntityType.ACCOUNT)).thenReturn("ACC-000001");
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(mockAccount);

        Account result = accountService.createAccount(mockAccount, 1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(AccountType.CREDIT, result.getType());
        Assertions.assertEquals("ACC-000001", result.getGlobalAccountId());
        Mockito.verify(accountRepository, Mockito.times(1)).save(mockAccount);
    }

    @Test
    void createAccountError() {
        Mockito.when(clientRepository.findById(1L)).thenReturn(Optional.empty());
        Account mockAccount = new Account(1L, "ACC-000001", AccountType.CREDIT,AccountStatus.OPEN, 1000.0, 0.0, null,null );
        Assertions.assertThrows(ClientException.class, () -> accountService.createAccount(mockAccount, 1L));
        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any(Account.class));
    }

    @Test
    void changeAccountStatus() {

        Account mockAccount = new Account(1L, "ACC-000001", AccountType.CREDIT,AccountStatus.OPEN, 1000.0, 0.0, null,null );
        Mockito.when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        accountService.changeAccountStatus(1L, AccountStatus.BLOCKED);

        Assertions.assertEquals(AccountStatus.BLOCKED, mockAccount.getStatus());
        Mockito.verify(accountRepository, Mockito.times(1)).save(mockAccount);
    }

    @Test
    void changeAccountStatusError() {
        Mockito.when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(AccountException.class, () -> accountService.changeAccountStatus(1L, AccountStatus.BLOCKED));
        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any(Account.class));
    }
}
