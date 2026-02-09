package com.quicktax.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QuicktaxApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuicktaxApplication.class, args);
    }
}
