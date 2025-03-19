package com.Task.Hasan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class HasanApplication {

	public static void main(String[] args) {
		SpringApplication.run(HasanApplication.class, args);
	}

}
