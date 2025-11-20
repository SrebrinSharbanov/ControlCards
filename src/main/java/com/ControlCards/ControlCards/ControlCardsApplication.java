package com.ControlCards.ControlCards;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.UserRepository;
import com.ControlCards.ControlCards.Util.Enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@Slf4j
public class ControlCardsApplication {

	public static void main(String[] args) {
		log.info("Starting ControlCards application...");
		SpringApplication.run(ControlCardsApplication.class, args);
		log.info("ControlCards application started successfully!");
	}

	@Bean
	@Profile("!test")
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
				admin.setActive(true);
				userRepository.save(admin);
				
				log.info("Admin user created successfully: {}", admin.getUsername());
			} else {
				userRepository.findByUsername("admin").ifPresent(admin -> {
					if (admin.getRole() == Role.ADMIN) {
						admin.setActive(true);
						userRepository.save(admin);
						log.info("Admin user ensured to be active: {}", admin.getUsername());
					}
				});
				log.info("Admin user already exists, skipping creation");
			}
		};
	}
}

