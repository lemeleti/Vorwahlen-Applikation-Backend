package ch.zhaw.vorwahlen;

import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinitionFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleDefinitionPartTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class to run the application
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({ModuleDefinitionFullTime.class, ModuleDefinitionPartTime.class})
public class VorwahlenApplication {

	/**
	 * Spring Boot entry point
	 */
	public static void main(String[] args) {
		SpringApplication.run(VorwahlenApplication.class, args);
	}
}
