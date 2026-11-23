package com.nijakaam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class NijakaamApplication {

	public static void main(String[] args) {
		SpringApplication.run(NijakaamApplication.class, args);
	}

	@Bean
	CommandLineRunner checkMongoDB(MongoTemplate mongoTemplate) {
		return args -> {
			try {
				mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
				System.out.println("✅ MongoDB Connected Successfully!");
			} catch (Exception e) {
				System.err.println("❌ MongoDB Connection Failed!");
				System.err.println(e.getMessage());
			}
		};
	}

}
