package com.sweta.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling

public class PortfolioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioBackendApplication.class, args);
        System.out.println("=================================");
        System.out.println("Portfolio Backend Started Successfully!");
        System.out.println("Swagger UI: http://localhost:8081/api/swagger-ui.html");
        System.out.println("=================================");
	}


}
