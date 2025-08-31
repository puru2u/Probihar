package com.probihar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ProbiharApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProbiharApplication.class, args);
		//System.out.println(new BCryptPasswordEncoder().encode("admin"));
	}

}
