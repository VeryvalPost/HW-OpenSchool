package ru.t1.java.service3.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.service3.dto.UnblockRequest;
import ru.t1.java.service3.dto.UnblockResponse;
import ru.t1.java.service3.service.ListUnblockService;

import java.util.List;


@RestController
@Slf4j
public class UnblockListController {

    ListUnblockService unblockService;

    public UnblockListController(ListUnblockService unblockService) {
        this.unblockService = unblockService;
    }

    @PostMapping("/api/unblock")
    public ResponseEntity<UnblockResponse> check(@RequestBody UnblockRequest unblockRequest) {

        log.info("Поступил перечень на разблокировку {}", unblockRequest.getGlobalIdList());
        List<String> unblockedList = unblockService.unblock(unblockRequest.getGlobalIdList());

        UnblockResponse response = UnblockResponse.builder()
                .unblockedList(unblockedList)
                .build();

        return ResponseEntity.ok(response);
    }



}
