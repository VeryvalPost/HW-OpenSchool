package ru.t1.java.demo.service.impl;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import ru.t1.java.demo.T1JavaDemoApplication;
import ru.t1.java.demo.web.CheckWebClientService;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = T1JavaDemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.config.location=classpath:application-test.yml"
)
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration.class,
})
@ActiveProfiles("test")
@AutoConfigureWireMock
@ComponentScan(basePackages = {
        "ru.t1.java.demo.service.impl",
        "ru.t1.java.demo.service",
        "ru.t1.java.demo.web",
        "ru.t1.java.demo.repository",
        "ru.t1.java.demo.model",
        "ru.t1.java.demo.config",
        "ru.t1.java.demo.aop",
        "ru.t1.java.demo.controller",
        "ru.t1.java.demo.model",
        "ru.t1.java.demo.kafka",
        "ru.t1.java.demo.util"
})
@EnableJpaRepositories(basePackages = "ru.t1.java.demo.repository")
@EntityScan(basePackages = "ru.t1.java.demo.model")
@TestPropertySource(properties = {
        "external.service.url=http://localhost:${wiremock.server.port}"})
class TransactionsOperatorIntegrationTest {
    @Autowired
    private CheckWebClientService webClientService;

    @Value("${wiremock.server.port}")
    private int wireMockPort;

    @Test
    void testCheckBlockedClientAtNewTransaction_ClientBlacklisted() {

        String globalClientId = "CLT-00001";
        String globalAccountId = "ACT-00001";

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-user", "Test")
        );

        stubFor(post(urlEqualTo("/api/checkClient"))
                .withRequestBody(equalToJson("{\"globalClientId\":\"CLT-00001\", \"globalAccountId\":\"ACT-00001\"}"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Authorization", equalTo("Bearer Test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"blocked\": true}")));

        boolean isBlacklisted = webClientService.isClientBlacklisted(globalClientId, globalAccountId);
        assertTrue(isBlacklisted);

        verify(postRequestedFor(urlEqualTo("/api/checkClient"))
                .withRequestBody(equalToJson("{\"globalClientId\":\"CLT-00001\", \"globalAccountId\":\"ACT-00001\"}"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Authorization", equalTo("Bearer Test")));

        System.out.println(WireMock.getAllServeEvents());
    }
}



