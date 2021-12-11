package ch.zhaw.vorwahlen.security.authentication;

import ch.zhaw.vorwahlen.security.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * This class is only used by {@link SecurityAdapter} to verify
 * if a provided user can access his dedicated path like: /students/student@zhaw.ch
 * and not paths of his/her fellow students like: /students/student2@zhaw.ch.
 * Only admins have the permission to view all paths.
 */
@Component("userSecurity")
public class UserSecurity {
    /**
     *
     * @param authentication {@link Authentication} containing {@link User}
     * @param studentMail restricted user path
     * @return true if user mail equals path variable mail or if user has role ADMIN else false
     */
    public boolean hasUserId(Authentication authentication, String studentMail) {
        var result = false;
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            result = CustomAuthProvider.ADMIN_ROLE.equals(user.getRole()) || studentMail.equals(user.getMail());
        }
        return result;
    }
}
