package ru.t1.java.service3;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ru.t1.java.service3")
@EntityScan(basePackages = {
        "ru.t1.java.service3.model"
})
@EnableJpaRepositories(basePackages = {
        "ru.t1.java.service3.repository"
})
@Profile("third")
public class Service3Application {

    public static void main(String[] args) {
        SpringApplication.run(Service3Application.class, args);
    }

}
