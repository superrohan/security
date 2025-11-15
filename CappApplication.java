package com.bank.capp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CappApplication {

    public static void main(String[] args) {
        SpringApplication.run(CappApplication.class, args);
    }
}
