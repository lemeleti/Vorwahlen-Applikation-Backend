package ch.zhaw.vorwahlen.authentication;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CustomAuthProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthToken authToken = (CustomAuthToken) authentication;

        if (authToken.getShibbolethSession() == null || authToken.getShibbolethSession().isEmpty()) {
            throw new SessionAuthenticationException("Session could not be created");
        }

        return new CustomAuthToken(new ArrayList<>(), authToken.getShibbolethSession(), authToken.getUser());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthToken.class.isAssignableFrom(authentication);
    }
}
