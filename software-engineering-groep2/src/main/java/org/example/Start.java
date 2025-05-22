package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Start {

    /** Accessible via: {@code http://localhost:8080} by default. */
    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}
