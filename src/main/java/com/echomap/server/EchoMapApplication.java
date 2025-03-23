package com.echomap.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.echomap.server", "com.echomap.server.controller"})
public class EchoMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(EchoMapApplication.class, args);
    }
}