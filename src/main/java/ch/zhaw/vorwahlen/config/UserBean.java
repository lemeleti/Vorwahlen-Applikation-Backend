package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.security.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserBean {
    @Bean
    public Optional<User> getUserFromSecurityContext() {
        return Optional.
                ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (User) auth.getPrincipal());
    }
}
