package com.pijourney.fileservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ApplicationConfig {
     @Bean
    public WebClient webClient(){
         return WebClient.builder()
                 .clientConnector(new ReactorClientHttpConnector()).build();
     }
     @Bean
    public ObjectMapper objectMapper(){
         return new ObjectMapper();
     }
}
