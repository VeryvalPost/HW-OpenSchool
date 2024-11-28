package ru.t1.java.demo.repository;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.AccountStatus;
import ru.t1.java.demo.model.Account;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("mainAccountRepository")
public interface AccountRepository extends JpaRepository<Account,Long> {

    Optional<Account> findAccountByGlobalAccountId (String globalId);
    @Query(value = "SELECT * FROM account WHERE status = :status LIMIT :limit", nativeQuery = true)
    List<Account> findTopNByStatus(@Param("status") AccountStatus status, @Param("limit") int limit);
}
