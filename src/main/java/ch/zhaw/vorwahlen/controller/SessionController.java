package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("session")
public class SessionController {
    @GetMapping(path = "info")
    public ResponseEntity<User> getSessionInfo() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "destroy")
    public ResponseEntity<Void> destroySession() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "is-admin")
    public ResponseEntity<Boolean> isUserAdmin() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(user.getRole().equals("ADMIN"));
    }

    @GetMapping(path = "/is-authenticated")
    public ResponseEntity<Boolean> isUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        try {
            if (auth != null && auth.getPrincipal() != null) {
                user = (User) auth.getPrincipal();
            }
        } catch (ClassCastException ignored) {
            // Has to be empty, do nothing
        }

        return ResponseEntity.ok(user != null);
    }
}
