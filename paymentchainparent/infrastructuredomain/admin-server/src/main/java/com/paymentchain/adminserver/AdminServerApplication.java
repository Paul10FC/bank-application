package com.paymentchain.adminserver;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@SpringBootApplication
@EnableAdminServer
@EnableEurekaClient
public class AdminServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServerApplication.class, args);
	}

		@Configuration
		public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter{
			@Override
			protected void configure(HttpSecurity security) throws Exception {
				security.authorizeRequests().anyRequest().permitAll()
						.and().csrf().disable();
			}
		}
	}
