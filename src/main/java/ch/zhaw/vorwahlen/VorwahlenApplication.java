package ch.zhaw.vorwahlen;

import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructurePartTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class to run the application
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({ModuleStructureFullTime.class, ModuleStructurePartTime.class})
public class VorwahlenApplication {

	/**
	 * Spring Boot entry point
	 */
	public static void main(String[] args) {
		SpringApplication.run(VorwahlenApplication.class, args);
	}
}
