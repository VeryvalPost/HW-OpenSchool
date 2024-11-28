package ru.t1.java.service3.service;

import ru.t1.java.service3.model.Transaction;

public interface TransactionOperator {
    void operate(String topic, Transaction transaction);
}