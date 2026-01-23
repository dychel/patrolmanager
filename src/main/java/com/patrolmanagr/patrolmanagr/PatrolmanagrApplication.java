package com.patrolmanagr.patrolmanagr;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PatrolmanagrApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatrolmanagrApplication.class, args);
	}
	@Bean
	public PasswordEncoder getBPE() {
		return new BCryptPasswordEncoder();
	}

}
