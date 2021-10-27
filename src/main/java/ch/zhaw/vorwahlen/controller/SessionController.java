package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the session of a {@link User}.
 */
@RestController
@RequestMapping("session")
public class SessionController {

    /**
     * Get the user information of the current session
     * @return {@link ResponseEntity<User>} with status code ok
     */
    @GetMapping(path = "info")
    public ResponseEntity<User> getSessionInfo() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(user);
    }

    /**
     * Destroy the current session.
     * @return {@link ResponseEntity<Void>} with status code ok
     */
    @GetMapping(path = "destroy")
    public ResponseEntity<Void> destroySession() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.ok().build();
    }

    /**
     * Says if the user in the current session is an admin.
     * @return {@link ResponseEntity<Boolean>} with status code ok
     */
    @GetMapping(path = "is-admin")
    public ResponseEntity<Boolean> isUserAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = getUserFromAuth(auth);
        return ResponseEntity.ok(user != null && "ADMIN".equals(user.getRole()));
    }

    /**
     * Says if the user in the current session is authenticated.
     * @return {@link ResponseEntity<Boolean>} with status code ok
     */
    @GetMapping(path = "/is-authenticated")
    public ResponseEntity<Boolean> isUserAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = getUserFromAuth(auth);
        return ResponseEntity.ok(user != null);
    }

    private User getUserFromAuth(Authentication auth) {
        User user = null;
        try {
            if (auth != null && auth.getPrincipal() != null) {
                user = (User) auth.getPrincipal();
            }
        } catch (ClassCastException ignored) {
            // Has to be empty, do nothing
        }
        return user;
    }

}
