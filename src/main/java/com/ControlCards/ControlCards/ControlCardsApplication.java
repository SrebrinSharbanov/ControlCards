package com.ControlCards.ControlCards;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.UserRepository;
import com.ControlCards.ControlCards.Util.Enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Slf4j
public class ControlCardsApplication {

	public static void main(String[] args) {
		log.info("Starting ControlCards application...");
		SpringApplication.run(ControlCardsApplication.class, args);
		log.info("ControlCards application started successfully!");
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			log.info("Initializing admin user...");
			
			if (userRepository.count() == 0) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin"));
				admin.setFirstName("Admin");
				admin.setLastName("Administrator");
				admin.setRole(Role.ADMIN);
				userRepository.save(admin);
				
				log.info("Admin user created successfully: {}", admin.getUsername());
			} else {
				log.info("Admin user already exists, skipping creation");
			}
		};
	}
}

