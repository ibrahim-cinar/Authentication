package com.cinar.authentication;

import com.cinar.authentication.model.Role;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.UserRepository;
import com.cinar.authentication.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singleton;

@SpringBootApplication

public class AuthenticationApplication {



    public static void main(String[] args) {
		SpringApplication.run(AuthenticationApplication.class, args);

	}

	}

