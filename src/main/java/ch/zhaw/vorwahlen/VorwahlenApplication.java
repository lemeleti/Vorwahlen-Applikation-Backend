package ch.zhaw.vorwahlen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class to run the application
 */
@SpringBootApplication
@EnableAsync
public class VorwahlenApplication {

	/**
	 * Spring Boot entry point
	 */
	public static void main(String[] args) {
		SpringApplication.run(VorwahlenApplication.class, args);
	}
}
