package com.microservices.demo.twitter.to.kafka.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwitterToKafkaServiceApplication implements CommandLineRunner {
    public static void main(String[] args)
    {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running application...");

    }
}
