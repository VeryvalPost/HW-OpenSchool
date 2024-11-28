package ru.t1.java.service2.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.service2.model.Account;

import java.util.Optional;

@Repository
@Qualifier("accountRepositoryTransactionService")
public interface AccountRepository extends JpaRepository<Account,Long> {

    Optional<Account> findAccountByGlobalAccountId (String globalId);

}
