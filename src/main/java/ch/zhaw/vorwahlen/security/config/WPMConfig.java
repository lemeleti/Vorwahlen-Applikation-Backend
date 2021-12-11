package ch.zhaw.vorwahlen.security.config;

import ch.zhaw.vorwahlen.security.authentication.AuthFilter;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.authentication.CustomAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

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
                "sessionId", "ABC123",
                "name", "dev",
                "lastName", "dev",
                "affiliation", "student;member",
                "homeOrg", "zhaw.ch",
                "mail", "dev@zhaw.ch",
                "role", CustomAuthProvider.ADMIN_ROLE
        );

        var filter = new AuthFilter(false, studentRepository);
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
        return new AuthFilter(true, studentRepository);
    }
}
