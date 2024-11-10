package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.DataSourceErrorLogDTO;
import ru.t1.java.demo.dto.MetricsDTO;
import ru.t1.java.demo.model.DataSourceErrorLog;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaMetricsProducer {
    @Autowired
    private final KafkaTemplate<String, MetricsDTO> template;

    public void send(String topic, String title, MetricsDTO metricsDTO) {
        try {
            log.debug("Отправляем сообщение в Kafka - Topic: {}, Заголовок: {}, Данные: {}", topic, title, metricsDTO);
            template.send(topic, title, metricsDTO);
        } catch (Exception ex) {
            log.error("Ошибка при отправке сообщения в Kafka: {}", ex.getMessage(), ex);
        } finally {
          template.flush();
        }
    }
}
