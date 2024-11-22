package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.EntityType;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.UniqueIdGeneratorService;
import ru.t1.java.demo.util.ClientMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final UniqueIdGeneratorService generatorService;

    @PostConstruct
    void init() {
        try {
            List<Client> clients = parseJson();
            clientRepository.saveAll(clients);
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }

    }

    @Override
//    @LogExecution
//    @Track
//    @HandlingResult
    public List<Client> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ClientDto[] clients = mapper.readValue(new File("src/main/resources/MOCK_DATA.json"), ClientDto[].class);

        return Arrays.stream(clients)
                .map(ClientMapper::toEntity)
                .collect(Collectors.toList());
    }


    @Override
    public Client registerClient(Client client) {
        try {
            String globalId = generatorService.generateId(EntityType.CLIENT);
            client.setGlobalId(globalId);
            client.setStatus(true);
            return clientRepository.save(client);
        } catch (DataAccessException e) {
            log.error("Ошибка обращения к базе данных при создании  клиента с ID: {}", client.getId(), e);
            throw new AccountException("Не получилось создать пользователя, ошибка БД:", e);
        }
    }
}
