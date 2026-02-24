package com.localmart.discovery_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @EnableEurekaServer is the single annotation that turns this plain Spring Boot app
 * into a Eureka registry. It activates the following REST endpoints:
 *
 *   POST   /eureka/apps/{appName}          — called by each service on startup to register
 *   DELETE /eureka/apps/{appName}/{id}     — called on graceful shutdown to deregister
 *   GET    /eureka/apps                    — returns all registered services (XML/JSON)
 *   PUT    /eureka/apps/{appName}/{id}     — heartbeat, called every 30s by each service
 *
 * The HTML dashboard is served at http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServerApplication.class, args);
	}

}
