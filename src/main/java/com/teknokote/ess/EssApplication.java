package com.teknokote.ess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.teknokote.pts.client", "com.teknokote.ess"})
@EnableFeignClients(basePackages = { "com.teknokote.pts.client","com.teknokote.ess.events.publish.cm" })
public class EssApplication{

	public static void main(String[] args) {

		SpringApplication.run(EssApplication.class, args);
	}
}
