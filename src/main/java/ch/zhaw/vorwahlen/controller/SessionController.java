package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.mapper.UserMapper;
import ch.zhaw.vorwahlen.model.dto.UserDTO;
import ch.zhaw.vorwahlen.security.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the session of a {@link User}.
 */
@RestController
@RequestMapping("session")
@RequiredArgsConstructor
public class SessionController {

    private final UserMapper mapper;

    /**
     * Get the user information of the current session.
     * @return {@link ResponseEntity} containing the {@link UserDTO}.
     */
    @GetMapping(path = "")
    public ResponseEntity<UserDTO> getSessionInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mapper.toDto(user));
    }

    /**
     * Says if the user in the current session is an admin.
     * @return {@link ResponseEntity} containing true or false.
     */
    @GetMapping(path = "is-admin")
    public ResponseEntity<Boolean> isUserAdmin(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user != null && "ADMIN".equals(user.getRole()));
    }

    /**
     * Says if the user in the current session is authenticated.
     * @return {@link ResponseEntity} containing true or false.
     */
    @GetMapping(path = "/is-authenticated")
    public ResponseEntity<Boolean> isUserAuthenticated(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user != null);
    }
}
