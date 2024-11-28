package ru.t1.java.service3.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.t1.java.service3.model.Client;

import java.util.List;
import java.util.Optional;
@Repository
@Qualifier("mainClientRepository")
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);
    Optional<Client> findClientByGlobalId (String globalId);
    @Query(value = "SELECT * FROM client a WHERE a.status = :status LIMIT :limit", nativeQuery = true)
    List<Client> findTopNByStatus(@Param("status") Boolean status, @Param("limit") int limit);
}