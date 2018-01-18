package com.taskboards.boards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class BoardsApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BoardsApplication.class, args);
	}
}
