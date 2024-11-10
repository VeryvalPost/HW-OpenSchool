package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.AccountDTO;
import ru.t1.java.demo.dto.ClientDto;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaAccountProducer<T extends AccountDTO> {

    private final KafkaTemplate <String, AccountDTO> template;


    public void sendTo(String topic, Object o) {
        try {

            template.send(MessageBuilder.withPayload(o)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, UUID.randomUUID().toString())
                    .build()).get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            template.flush();
        }
    }



}
