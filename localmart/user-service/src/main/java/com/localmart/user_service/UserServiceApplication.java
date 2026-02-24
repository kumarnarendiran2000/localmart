package com.localmart.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		// Set JVM default timezone to UTC BEFORE Spring Boot starts.
		// The PostgreSQL JDBC driver sends the JVM timezone to PostgreSQL during
		// the TCP connection handshake. On Windows with India locale, JVM reports
		// "Asia/Calcutta" (deprecated timezone name) which PostgreSQL 16 rejects.
		// Setting UTC here ensures the JDBC driver sends a valid timezone,
		// and all timestamps are stored/read in UTC (production best practice).
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		SpringApplication.run(UserServiceApplication.class, args);
	}

}
