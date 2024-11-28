package ru.t1.java.service3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountStatus;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j

public class ListUnblockService {
    @Autowired
    @Qualifier("clientRepositoryBlockService")
    private final ClientRepository clientRepository;
    @Autowired
    @Qualifier("accountRepositoryBlockService")
    private final AccountRepository accountRepository;

    public ListUnblockService(ClientRepository clientRepository, AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
    }


    public List<String> unblock(List<String> listId) {

        if (listId.isEmpty()){
            return listId;
        }

        List<String> unblockedListId = new ArrayList<>();

        if (listId.get(0).contains("CLT")){
            List<Client> unblockedList = createUnblockedList();

            for (String globalId: listId){
                Optional<Client> clientOpt = clientRepository.findClientByGlobalId(globalId);
                if (clientOpt.isPresent()){
                    Client client = clientOpt.get();
                    client.setStatus(true); // Здесь я решил, что все клиенты, которые попадают в руки
                    //сервису должны быть разблокированы.
                    unblockedList.add(client);
                }
            }

            clientRepository.saveAll(unblockedList);
             unblockedListId = unblockedList.stream()
                    .map(Client::getGlobalId)
                    .toList();

        } else if (listId.get(0).contains("ACT")){
            List<Account> unblockedList = createUnblockedList();

            for (String globalId: listId){
                Optional<Account> accountOpt = accountRepository.findAccountByGlobalAccountId(globalId);
                if (accountOpt.isPresent()){
                    Account account = accountOpt.get();
                    account.setStatus(AccountStatus.OPEN); // Здесь я решил, что все аккаунты, которые попадают в руки
                                                            //сервису должны быть разблокированы.
                    unblockedList.add(account);
                }
            }

            accountRepository.saveAll(unblockedList);
             unblockedListId = unblockedList.stream()
                    .map(Account::getGlobalAccountId)
                    .toList();
        }

        return  unblockedListId;
    }


    public <T> List<T> createUnblockedList() {
        return new ArrayList<>();
    }

}
