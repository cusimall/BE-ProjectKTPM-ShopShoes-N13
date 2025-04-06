package com.microservice.emailsenderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EmailsenderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailsenderServiceApplication.class, args);
	}

}
