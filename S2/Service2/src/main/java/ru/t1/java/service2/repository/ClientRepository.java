package ru.t1.java.service2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.java.service2.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);
    Optional<Client> findClientByGlobalId (String globalId);
}