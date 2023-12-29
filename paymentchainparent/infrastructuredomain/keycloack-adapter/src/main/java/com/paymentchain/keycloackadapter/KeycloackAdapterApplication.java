package com.paymentchain.keycloackadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class KeycloackAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeycloackAdapterApplication.class, args);
	}

}
