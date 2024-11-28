package ru.t1.java.service2.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.java.service2.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("transactionRepositoryTransactionService")
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findAllTransactionByGlobalAccountId (String globalAccountId);
    Optional<Transaction> findTransactionByGlobalTransactionId (String globalTransactionId);

    @Query("SELECT t FROM Transaction t WHERE t.account.globalAccountId = :globalAccountId AND t.timestamp >= :startTime")
    List<Transaction> findLastTransactions(
            @Param("globalAccountId") String globalAccountId,
            @Param("startTime") LocalDateTime startTime
    );

}
