package com.ControlCards.ControlCards;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.UserRepository;
import com.ControlCards.ControlCards.Util.Enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ControlCardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlCardsApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.count() == 0) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin"));
				admin.setFirstName("Admin");
				admin.setLastName("Administrator");
				admin.setRole(Role.ADMIN);
				userRepository.save(admin);
			}
		};
	}

}
