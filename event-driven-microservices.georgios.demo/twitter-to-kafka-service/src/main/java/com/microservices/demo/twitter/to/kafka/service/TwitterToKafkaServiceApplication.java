package com.microservices.demo.twitter.to.kafka.service;

import com.microservices.demo.config.TwitterToKafkaServiceConfig;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo") // So that we can find beans in other modules
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);
    private final TwitterToKafkaServiceConfig configuration;

    private final StreamRunner streamRunner;

    public TwitterToKafkaServiceApplication(TwitterToKafkaServiceConfig configuration, StreamRunner streamRunner) {
        this.configuration = configuration;
        this.streamRunner = streamRunner;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        LOG.info("Running application...");
        LOG.info(Arrays.toString(configuration.getTwitterKeywords().toArray(new String[0])));
        LOG.info(configuration.getWelcomeMessage());
        streamRunner.start();
    }
}
