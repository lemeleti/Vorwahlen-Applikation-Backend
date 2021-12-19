package ch.zhaw.vorwahlen.security.config;

import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.authentication.AuthFilter;
import ch.zhaw.vorwahlen.security.authentication.CustomAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

import static ch.zhaw.vorwahlen.security.authentication.AuthFilter.HeaderAttribute.*;

/**
 * Profile configuration for running the application in development or production mode.
 */
@Configuration
@RequiredArgsConstructor
public class WPMConfig {
    private final StudentRepository studentRepository;

    /**
     * Run the application with {@link AuthFilter} in development mode.
     * @return the AuthFilter with the dev user.
     */
    @Profile("dev")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter developmentAuthFilter() {
        var userData = Map.of(
            SHIB_SESSION_ID, "ABC123",
            GIVEN_NAME, "dev",
            SURNAME, "dev",
            AFFILIATION, "student;member",
            HOME_ORGANIZATION, "zhaw.ch",
            MAIL, "dev@zhaw.ch"
        );

        var filter = new AuthFilter(false, studentRepository);
        filter.setTestingUserData(userData);
        return filter;
    }

    /**
     * Run the application with the {@link AuthFilter} in production mode
     * @return the AuthFilter
     */
    @Profile("prod")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter productionAuthFilter() {
        return new AuthFilter(true, studentRepository);
    }
}
