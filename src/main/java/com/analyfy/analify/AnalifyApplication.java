package com.analyfy.analify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnalifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalifyApplication.class, args);
	}

}
	