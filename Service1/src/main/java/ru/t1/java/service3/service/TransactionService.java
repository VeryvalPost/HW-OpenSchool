package ru.t1.java.service3.service;

import ru.t1.java.service3.model.Transaction;

import java.io.IOException;
import java.util.List;

public interface TransactionService {

    List<Transaction> parseJson() throws IOException;

    void sendTransactionToKafka(String topic, Transaction transaction);
    Transaction createTransaction();
    String blockTransaction(Transaction transaction);

}
