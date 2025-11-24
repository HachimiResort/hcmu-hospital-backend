package org.hcmu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HcmuServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HcmuServerApplication.class, args);
	}

}
