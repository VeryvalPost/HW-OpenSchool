package ru.t1.java.service2.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.t1.java.service2.model.Client;
import ru.t1.java.service2.repository.ClientRepository;
import ru.t1.java.service2.util.BlackListGenerator;


import java.util.Optional;

@Service
@Slf4j

public class ClientAcceptService {

    @Qualifier("clientRepositoryTransactionService")
    private final ClientRepository clientRepository;

    public ClientAcceptService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }


    public boolean checkClientBlocked(String globalClientId) {
        boolean isBlocked = false;

        // Заглушка для имитации обычного блэклиста.
        // Теоретически можно было создать еще одну таблицу в БД и связать с клиентами
        BlackListGenerator generator = new BlackListGenerator();
        isBlocked = generator.generate(globalClientId);

        return isBlocked;
    }

}
