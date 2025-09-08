package com.sweta.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling

public class PortfolioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioBackendApplication.class, args);
		log.info("=================================");
	    log.info("Portfolio Backend Started Successfully!");
	    log.info("Swagger UI: http://localhost:8081/api/swagger-ui.html");
	    log.info("=================================");

	}
}
