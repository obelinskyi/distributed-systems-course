package com.lohika.course.bfffrontend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class BffFrontendApplication {

    @Value("${authors.url}")
    private String authorsUrl;

    @Value("${books.url}")
    private String booksUrl;

    public static void main(String[] args) {
        SpringApplication.run(BffFrontendApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebClient authorClient() {
        return WebClient
                .builder()
                .baseUrl(authorsUrl)
                .build();
    }

    @Bean
    public WebClient bookClient() {
        return WebClient
                .builder()
                .baseUrl(booksUrl)
                .build();
    }
}
