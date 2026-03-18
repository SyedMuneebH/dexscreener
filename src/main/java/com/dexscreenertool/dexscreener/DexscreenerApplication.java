package com.dexscreenertool.dexscreener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DexscreenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DexscreenerApplication.class, args);
	}

}
