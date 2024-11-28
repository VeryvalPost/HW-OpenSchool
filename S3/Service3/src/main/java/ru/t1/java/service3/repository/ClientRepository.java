package ru.t1.java.service3.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.service3.model.Client;


import java.util.Optional;
@Repository
@Qualifier("clientRepositoryBlockService")
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);
    Optional<Client> findClientByGlobalId (String globalId);
}