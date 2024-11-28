package ru.t1.java.service3.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.t1.java.service3.service.UnblockService;

import java.util.List;

@Component
@Slf4j
public class UnblockScheduler {

     private final List<UnblockService> unblockServices;

    @Autowired
    public UnblockScheduler(List<UnblockService> unblockServices) {
        this.unblockServices = unblockServices;
    }

    @Scheduled(fixedRateString = "${unblock.period}")
    public void runUnblockTask() {
        log.info("Начало работы сбора данных по блокировкам");
        for (UnblockService service : unblockServices) {
            service.collectUnblockList();
        }
    }
}