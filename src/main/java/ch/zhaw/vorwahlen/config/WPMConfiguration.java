package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.authentication.AuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
public class WPMConfiguration {
    @Profile("dev")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter developmentAuthFilter() {
        Map<String, String> userData = Map.of(
                "sessionId", "ABC123",
                "name", "dev",
                "lastName", "dev",
                "affiliation", "student;member",
                "homeOrg", "zhaw.ch",
                "mail", "dev@zhaw.ch"
        );

        AuthFilter filter = new AuthFilter(false);
        filter.setUserData(userData);
        return filter;
    }

    @Profile("prod")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter productionAuthFilter() {
        return new AuthFilter(true);
    }

}
