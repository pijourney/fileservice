package com.pijourney.fileservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(SpringExtension.class)

@SpringBootTest(classes ={FileserviceApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileserviceApplicationTests {

	@Test
	void contextLoads() {
	}
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
