package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.model.*;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.UniqueIdGeneratorService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UniqueIdGeneratorService idGenerator;

    @InjectMocks
    private AccountServiceImpl accountService;
    @Test
    void createAccount() {

        String globalClientId = "CLT-00000001";
        String globalAccountId = "ACT-00000001";

        Client client = new Client();
        client.setGlobalId(globalClientId);

        Account account = new Account();
        account.setClient(client);


        Mockito.when(clientRepository.findClientByGlobalId(globalClientId))
                .thenReturn(Optional.of(client));
        Mockito.when(idGenerator.generateId(EntityType.ACCOUNT))
                .thenReturn(globalAccountId);
        Mockito.when(accountRepository.save(account))
                .thenReturn(account);


        Account result = accountService.createAccount(account, globalClientId);


        Assertions.assertNotNull(result);
        Assertions.assertEquals(globalAccountId, result.getGlobalAccountId());
        Assertions.assertEquals(client, result.getClient());
        Mockito.verify(clientRepository).findClientByGlobalId(globalClientId);
        Mockito.verify(idGenerator).generateId(EntityType.ACCOUNT);
        Mockito.verify(accountRepository).save(account);

    }

    @Test
    void changeAccountStatus() {

        String globalAccountId = "ACT-00000001";
        AccountStatus newStatus = AccountStatus.BLOCKED;
        Account account = new Account();
        account.setGlobalAccountId(globalAccountId);
        account.setStatus(AccountStatus.OPEN);

        Mockito.when(accountRepository.findAccountByGlobalAccountId(globalAccountId))
                .thenReturn(Optional.of(account));
        Mockito.when(accountRepository.save(account))
                .thenReturn(account);

        accountService.changeAccountStatus(globalAccountId, newStatus);

        Assertions.assertEquals(newStatus, account.getStatus());
        Mockito.verify(accountRepository).findAccountByGlobalAccountId(globalAccountId);
        Mockito.verify(accountRepository).save(account);
    }


    @Test
    void changeAccountStatus_accountNotFound_shouldThrowAccountException() {

        String globalAccountId = "ACT-00000001";
        AccountStatus newStatus = AccountStatus.BLOCKED;

        Mockito.when(accountRepository.findAccountByGlobalAccountId(globalAccountId))
                .thenReturn(Optional.empty());

        AccountException thrown = Assertions.assertThrows(AccountException.class, () -> {
            accountService.changeAccountStatus(globalAccountId, newStatus);
        });

        Assertions.assertEquals("Аккаунт не найден ID" + globalAccountId, thrown.getMessage());
        Mockito.verify(accountRepository).findAccountByGlobalAccountId(globalAccountId);
        Mockito.verify(accountRepository, Mockito.never()).save(Mockito.any());
    }


    @Test
    void changeBalance() {
        String globalTransactionId = "TRX-00000001";
        double transactionAmount = 100.0;

        Account account = new Account();
        account.setId(1L);
        account.setGlobalAccountId("ACT-00000001");
        account.setBalance(200.0);

        Transaction transaction = new Transaction();
        transaction.setGlobalTransactionId(globalTransactionId);
        transaction.setAmount(transactionAmount);
        transaction.setAccount(account);

        Mockito.when(accountRepository.findAccountByGlobalAccountId("ACT-00000001"))
                .thenReturn(Optional.of(account));
        Mockito.when(accountRepository.save(account))
                .thenReturn(account);

        accountService.changeBalance(transaction);

        Assertions.assertEquals(300.0, account.getBalance());
        Mockito.verify(accountRepository).findAccountByGlobalAccountId("ACT-00000001");
        Mockito.verify(accountRepository).save(account);
    }
}