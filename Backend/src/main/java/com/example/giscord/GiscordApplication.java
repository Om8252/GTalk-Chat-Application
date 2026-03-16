package com.example.giscord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GiscordApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiscordApplication.class, args);
	}

}
