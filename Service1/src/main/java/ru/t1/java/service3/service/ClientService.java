package ru.t1.java.service3.service;

import ru.t1.java.service3.model.Client;

import java.io.IOException;
import java.util.List;

public interface ClientService {
    List<Client> parseJson() throws IOException;
    Client registerClient(Client client);
    void blockClient(String globalClientId);
    void unBlockClient(String globalClientId);
}
