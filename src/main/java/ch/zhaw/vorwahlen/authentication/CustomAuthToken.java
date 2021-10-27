package ch.zhaw.vorwahlen.authentication;

import ch.zhaw.vorwahlen.model.user.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class contains the current user and session.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class CustomAuthToken extends AbstractAuthenticationToken {
    private final String shibbolethSession;
    private final User user;

    /**
     * Create instance.
     * @param shibbolethSession Current Session from Shibboleth for this user.
     * @param user The user trying to authenticate.
     */
    public CustomAuthToken(String shibbolethSession, User user) {
        super(new ArrayList<>());
        this.shibbolethSession = shibbolethSession;
        this.user = user;
        super.setAuthenticated(false);
    }

    /**
     * Create instance.
     * @param authorities GrantedAuthority for this user.
     * @param shibbolethSession Current Session from Shibboleth for this user.
     * @param user The user trying to authenticate.
     */
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
