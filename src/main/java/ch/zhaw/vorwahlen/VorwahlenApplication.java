package ch.zhaw.vorwahlen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VorwahlenApplication{
	public static void main(String[] args) {
		SpringApplication.run(VorwahlenApplication.class, args);
	}
}
