package com.microservices.demo.twitter.to.kafka.service;

import com.microservices.demo.twitter.to.kafka.service.serviceconfig.TwitterToKafkaServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);
    private final TwitterToKafkaServiceConfig configuration;

    public TwitterToKafkaServiceApplication(TwitterToKafkaServiceConfig configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        LOG.info("Running application...");
        LOG.info(Arrays.toString(configuration.getTwitterKeywords().toArray(new String[]{})));
        LOG.info(configuration.getWelcomeMessage());
    }
}
