package io.github.ripliquid.cloudops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                CloudopsApplication.class,
                args
        );
    }
}