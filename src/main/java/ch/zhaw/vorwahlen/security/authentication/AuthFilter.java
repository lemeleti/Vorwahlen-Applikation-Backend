package ch.zhaw.vorwahlen.security.authentication;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.model.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class filters the request and response based on the {@link CustomAuthToken}.
 * If the Authentication does not exist it will be created with the user provided in the request.
 */
@RequiredArgsConstructor
@Setter
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private final boolean isProd;
    private final StudentRepository studentRepository;
    private Map<String, String> userData = new HashMap<>();
    private Student student;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (isProd) {
            extractUserInfoFromHeader(request);
        }
        findAndSetStudent(userData.get("mail"));

        if (isUserDataNotNull() && (auth == null || !auth.isAuthenticated())) {
            auth = new CustomAuthToken(userData.get("sessionId"), createUser());
            log.debug("received login request from {}", userData.get("mail"));
            log.debug(userData.toString());
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private boolean isUserDataNotNull() {
        for (var s : userData.values()) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void extractUserInfoFromHeader(HttpServletRequest request) {
        userData.put("sessionId", URLDecoder.decode(request.getHeader("shib-session-id"), StandardCharsets.UTF_8));
        userData.put("name", URLDecoder.decode(request.getHeader("givenname"), StandardCharsets.UTF_8));
        userData.put("lastName", URLDecoder.decode(request.getHeader("surname"), StandardCharsets.UTF_8));
        userData.put("affiliation", URLDecoder.decode(request.getHeader("affiliation"), StandardCharsets.UTF_8));
        userData.put("homeOrg", URLDecoder.decode(request.getHeader("homeorganization"), StandardCharsets.UTF_8));
        userData.put("mail", URLDecoder.decode(request.getHeader("mail"), StandardCharsets.UTF_8));
        userData.put("role", "USER");
    }

    private User createUser() {
        return User.builder()
                .name(userData.get("name"))
                .lastName(userData.get("lastName"))
                .affiliation(userData.get("affiliation"))
                .homeOrg(userData.get("homeOrg"))
                .mail(userData.get("mail"))
                .role(userData.get("role"))
                .isExistent(student != null)
                .build();
    }

    private void findAndSetStudent(String email) {
        if (email != null && !email.isBlank()) {
            var studentOptional = studentRepository.findById(userData.get("mail"));
            studentOptional.ifPresent(value -> student = value);
        }
    }
}
