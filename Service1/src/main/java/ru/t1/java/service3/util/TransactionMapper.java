package ru.t1.java.service3.util;

import org.springframework.stereotype.Component;
import ru.t1.java.service3.dto.TransactionDTO;
import ru.t1.java.service3.model.Account;
import ru.t1.java.service3.model.Transaction;



@Component
public class TransactionMapper {

    public static Transaction toEntity(TransactionDTO dto, Account account) {
        if (dto == null) {
            return null;
        }

        return Transaction.builder()
                .globalTransactionId(dto.getGlobalTransactionId())
                .amount(dto.getAmount())
                .timestamp(dto.getTimestamp())
                .account(account)
                .build();
    }

    public static TransactionDTO toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionDTO.builder()
                .globalTransactionId(transaction.getGlobalTransactionId())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .globalAccountId(transaction.getAccount().getGlobalAccountId())
                .accountBalance(transaction.getAccount().getBalance())
                .clientId(transaction.getAccount().getClient().getId())
                .build();
    }
}
