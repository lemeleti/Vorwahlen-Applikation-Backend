package ch.zhaw.vorwahlen.authentication;

import ch.zhaw.vorwahlen.model.user.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@EqualsAndHashCode(callSuper = false)
public class CustomAuthToken extends AbstractAuthenticationToken {
    private final String shibbolethSession;
    private final User user;

    public CustomAuthToken(String shibbolethSession, User user) {
        super(new ArrayList<>());
        this.shibbolethSession = shibbolethSession;
        this.user = user;
        super.setAuthenticated(false);
    }

    public CustomAuthToken(Collection<? extends GrantedAuthority> authorities, String shibbolethSession, User user) {
        super(authorities);
        this.shibbolethSession = shibbolethSession;
        this.user = user;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }
}
