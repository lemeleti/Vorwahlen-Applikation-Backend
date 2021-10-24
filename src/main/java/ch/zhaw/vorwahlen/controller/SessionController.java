package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.user.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("session")
public class SessionController {

    @GetMapping(path = "/init")
    public ResponseEntity<Map<String, String>> createSession(@RequestHeader HttpHeaders headers) {
        Map<String, String> headerMap = new HashMap<>();
        headers.forEach((s1, s2) -> headerMap.put(s1, s2.toString()));
        // Todo auf einen richtigen Endpoint weiterleiten statt Daten anzuzeigen.
        return ResponseEntity.ok(headerMap);
    }

    @GetMapping(path = "info")
    public ResponseEntity<User> getSessionInfo() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(user);
    }
}
