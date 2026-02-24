package com.localmart.order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

// @EnableFeignClients: tells Spring to scan for @FeignClient interfaces at startup
// and generate proxy implementations for each one.
// Without this annotation, Spring ignores all @FeignClient interfaces entirely.
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
