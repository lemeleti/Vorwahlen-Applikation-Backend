package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.authentication.AuthFilter;
import ch.zhaw.vorwahlen.authentication.CustomAuthToken;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.modulevalidation.ElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.PartTimeElectionValidator;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * Profile configuration for running the application in development or production mode.
 */
@Configuration
@RequiredArgsConstructor
public class WPMConfig {
    private final ClassListRepository classListRepository;

    /**
     * Run the application with {@link AuthFilter} in development mode.
     * @return the AuthFilter with the dev user.
     */
    @Profile("dev")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter developmentAuthFilter() {
        var userData = Map.of(
                "sessionId", "ABC123",
                "name", "dev",
                "lastName", "dev",
                "affiliation", "student;member",
                "homeOrg", "zhaw.ch",
                "mail", "dev@zhaw.ch",
                "role", "ADMIN"
        );

        var filter = new AuthFilter(false, classListRepository);
        var student = new Student();
        student.setEmail(userData.get("mail"));
        filter.setStudent(student);
        filter.setUserData(userData);
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
        return new AuthFilter(true, classListRepository);
    }
}
