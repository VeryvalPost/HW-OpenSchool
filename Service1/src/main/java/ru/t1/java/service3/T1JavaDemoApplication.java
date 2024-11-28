package ru.t1.java.service3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ru.t1.java.demo")
@EntityScan(basePackages = {
        "ru.t1.java.demo.model"
})
@EnableJpaRepositories(basePackages = {
        "ru.t1.java.demo.repository"
})
@Slf4j
@Profile("main")
public class T1JavaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(T1JavaDemoApplication.class, args);
    }

}
