package com.online_library_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OnlineLibraryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineLibraryServiceApplication.class, args);
	}

}
