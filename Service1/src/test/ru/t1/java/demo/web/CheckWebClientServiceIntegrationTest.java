package ru.t1.java.demo.web;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.java.demo.T1JavaDemoApplication;
import ru.t1.java.demo.exception.ExternalServiceException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = T1JavaDemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration.class,
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0) // Автоматически настроит WireMock на случайный порт
@EnableJpaRepositories(basePackages = "ru.t1.java.demo.repository")
@EntityScan(basePackages = "ru.t1.java.demo.model")
@TestPropertySource(properties = "security.token=samiyNadejniyTokenSrazuNad0ZamenitNaChtoN1budNormalnoe")
@TestPropertySource(properties = "security.expiration=3600")
@TestPropertySource(properties = {
        "external.service.url=http://localhost:${wiremock.server.port}"
})
/*
@TestPropertySource(properties = {
        "security.token=samiyNadejniyTokenSrazuNad0ZamenitNaChtoN1budNormalnoe",
        "security.expiration=3600",
        "external.service.url=http://localhost:${wiremock.server.port}",
        "spring.kafka.topic.transactions = t1_demo_transactions",
        "spring.kafka.topic.accounts= t1_demo_accounts",
        "spring.kafka.topic.transactionsAccept = t1_demo_transaction_accept",
        "spring.kafka.topic.transactionsResult = t1_demo_transaction_result"
})
 */
class CheckWebClientServiceIntegrationTest {

    @Autowired
    private CheckWebClientService checkWebClientService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${wiremock.server.port}")
    private int wireMockPort;

    @Test
    void testIsClientBlacklisted_ClientBlocked() {
        stubFor(post(urlEqualTo("/api/checkClient"))
                .withRequestBody(equalToJson("{\"globalClientId\":\"CLT-00000001\", \"globalAccountId\":\"ACT-00000001\"}"))
                .withHeader("Authorization", equalTo("Bearer Test"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody("{\"blocked\": false}")));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-user", "Test")
        );



        WebClient webClient = webClientBuilder.baseUrl("http://localhost:" + wireMockPort).build();
        boolean isBlocked = checkWebClientService.isClientBlacklisted("CLT-00000001", "ACT-00000001");

        assertFalse(isBlocked);

        verify(postRequestedFor(urlEqualTo("/api/checkClient"))
                .withRequestBody(equalToJson("{\"globalClientId\":\"CLT-00000001\", \"globalAccountId\":\"ACT-00000001\"}"))
                .withHeader("Authorization", equalTo("Bearer Test")));

        System.out.println(WireMock.getAllServeEvents());
    }
    @Test
    void testIsClientBlacklisted_ExternalServiceError() {

        stubFor(post(urlEqualTo("/api/checkClient"))
                .withRequestBody(equalToJson("{\"globalClientId\":\"CLT-00000001\", \"globalAccountId\":\"ACT-00000001\"}"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-user", "Test")
        );

        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> checkWebClientService.isClientBlacklisted("CLT-00000001", "ACT-00000001"));

        assertEquals("Ошибка при проверке клиента. Код: 500", exception.getMessage());
        System.out.println(WireMock.getAllServeEvents());
    }
}
