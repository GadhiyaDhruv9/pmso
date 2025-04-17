package com.pmso.projectManagementSystemOne;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.pmso.projectManagementSystemOne.repository")
public class ProjectManagementSystemOneApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProjectManagementSystemOneApplication.class, args);
	}
}