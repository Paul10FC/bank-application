package com.paymentchain.customer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableEurekaClient
public class CustomerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}
	@Bean
	public OpenAPI customOpenApi(){
		return new OpenAPI()
				.components(new Components())
				.info(new Info()
						.title("Customer API")
						.version("1.0.0"));
	}
}
