package com.microservices.demo.twitter.to.kafka.service.serviceconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data   // This is lombok related. During compile time it will create boilerplate getters/setters for fields of the class.
        // These will only be present in the byte-code to be accessed by jvm.
@Configuration
@ConfigurationProperties(prefix = "twitter-to-kafka-service")
public class TwitterToKafkaServiceConfig {
    private List<String> twitterKeywords;   // The Name has to match that of the configuration entry in the yml file.
    // twitter-keywords -> twitterKeywords. So the convention is that "-k" -> K.
    private String welcomeMessage;  // Matches welcome-message in application.yml
    private Boolean enableMockTweets;
    private Long mockSleepMs;
    private Integer mockMinTweetLength;
    private Integer mockMaxTweetLength;
}
