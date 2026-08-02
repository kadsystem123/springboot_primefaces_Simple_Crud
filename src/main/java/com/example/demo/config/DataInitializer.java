package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

/**
 * ينشئ مستخدماً مسؤولاً (ADMIN) افتراضياً عند أول تشغيل إذا لم يكن موجوداً،
 * لتسهيل الاختبار: admin / Admin@123
 */
@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByUsername("admin")) {
				User admin = new User("admin", passwordEncoder.encode("Admin@123"), "admin@example.com", Role.ADMIN);
				userRepository.save(admin);
			}
		};
	}
}
