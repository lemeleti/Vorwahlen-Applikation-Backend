package ch.zhaw.vorwahlen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class overrides the default cors configuration.
 */
@Configuration
@EnableWebMvc
public class CORSAdvice implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "PUT", "OPTIONS", "DELETE", "POST", "PATCH")
                .allowedOrigins("http://localhost:8081", "http://localhost", "http://vorwahlen.cloudlab.zhaw.ch",
                                "https://vorwahlen.cloudlab.zhaw.ch")
                .allowedHeaders("Origin", "Content-Type", "Accept");
    }

}