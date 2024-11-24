package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountStatus;
import ru.t1.java.demo.model.Transaction;

import java.io.IOException;
import java.util.List;

public interface AccountService {
    Account createAccount(Account account, String globalClientId);
    Account updateAccount(String globalAccountId, Account updatedAccount) ;
    void changeAccountStatus(String globalAccountId, AccountStatus status);
    List<Transaction> findAllAccountTransactions(String globalAccountId);

    List<Account> parseJson() throws IOException;

    void changeBalance(Transaction transaction);

    // Сделано для тестирования producer и consumer Kafka
    void sendAccountToKafka() ;
/*
К заданию №4
 */
    void blockAccount(String globalAccountId);
}
