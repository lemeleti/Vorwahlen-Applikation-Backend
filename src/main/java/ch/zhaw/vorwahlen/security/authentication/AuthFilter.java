package ch.zhaw.vorwahlen.security.authentication;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.model.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class filters the request and response based on the {@link CustomAuthToken}.
 * If the Authentication does not exist it will be created with the user provided in the request.
 */
@RequiredArgsConstructor
@Setter
@Log
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
        userData.put("sessionId", request.getHeader("shib-session-id"));
        userData.put("name", request.getHeader("givenname"));
        userData.put("lastName", request.getHeader("surname"));
        userData.put("affiliation", request.getHeader("affiliation"));
        userData.put("homeOrg", request.getHeader("homeorganization"));
        userData.put("mail", request.getHeader("mail"));
        userData.put("role", "USER");

        var headerNames = request.getHeaderNames();
        var stringBuilder = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            var header = headerNames.nextElement();
            stringBuilder.append(String.format("(%s='%s')", header, request.getHeader(header)));

        }
        log.info(String.format("Erhaltene Loginheader: %s", stringBuilder.toString()));
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
