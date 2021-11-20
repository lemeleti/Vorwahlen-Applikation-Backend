package ch.zhaw.vorwahlen.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * This class provides the authentication token with the correct roles and permissions.
 */
@Component
@PropertySource("classpath:settings.properties")
public class CustomAuthProvider implements AuthenticationProvider {
    private static final String ADMIN_ROLE = "ADMIN";

    private final String[] admins;

    /**
     * Create instance.
     * @param admins Admins from the environment variable "ADMIN"
     */
    public CustomAuthProvider(@Value("${admin}") String[] admins) {
        this.admins = admins;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var authToken = (CustomAuthToken) authentication;
        var authorities = new ArrayList<GrantedAuthority>();

        if (authToken.getShibbolethSession() == null || authToken.getShibbolethSession().isEmpty()) {
            throw new SessionAuthenticationException("Session could not be created");
        }

        var user = authToken.getUser();
        for (var admin : admins) {
            if (user.getMail().equals(admin)) {
                authorities.add(new SimpleGrantedAuthority(ADMIN_ROLE));
                user.setRole(ADMIN_ROLE);
            }
        }

        if (user.getStudent() == null && !ADMIN_ROLE.equals(user.getRole())) {
            return authentication;
        }

        return new CustomAuthToken(authorities, authToken.getShibbolethSession(), user);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthToken.class.isAssignableFrom(authentication);
    }
}
