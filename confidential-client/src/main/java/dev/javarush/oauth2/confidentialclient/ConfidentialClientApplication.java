package dev.javarush.oauth2.confidentialclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ConfidentialClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfidentialClientApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(
			RestTemplateBuilder builder
	) {
		return builder.build();
	}

}
