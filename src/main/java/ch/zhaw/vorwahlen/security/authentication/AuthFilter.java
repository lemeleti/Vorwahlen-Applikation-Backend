package ch.zhaw.vorwahlen.security.authentication;

import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.StudentRepository;
import ch.zhaw.vorwahlen.security.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
 * This class filters the request and response based on the {@link UsernamePasswordAuthenticationToken}.
 * If the Authentication does not exist it will be created with the user provided in the request.
 */
@RequiredArgsConstructor
@Setter
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private enum HeaderAttribute {
        SHIB_SESSION_ID("shib-session-id"),
        GIVEN_NAME("givenname"),
        SURNAME("surname"),
        AFFILIATION("affiliation"),
        HOME_ORGANIZATION("homeorganization"),
        MAIL("mail");

        @Getter
        private final String name;

        HeaderAttribute(String name) {
            this.name = name;
        }
    }
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
            //auth = new CustomAuthToken(userData.get("sessionId"), createUser());
            auth = new UsernamePasswordAuthenticationToken(createUser(), null);
            log.debug("received login request from {}", userData.get("mail"));
            log.debug(userData.toString());
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private boolean isUserDataNotNull() {
        var userDataValues = userData.values();
        if (userDataValues.size() > 1) {
            for (var s : userData.values()) {
                if (s == null || s.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void extractUserInfoFromHeader(HttpServletRequest request) {
        for (var headerAttribute: HeaderAttribute.values()) {
            var headerValue = request.getHeader(headerAttribute.name);
            if (headerValue != null) {
                var fieldName = fieldNameForHeaderAttribute(headerAttribute);
                userData.put(fieldName, URLDecoder.decode(headerValue, StandardCharsets.UTF_8));
            }
        }
        userData.put("role", "USER");
    }

    private User createUser() {
        return User.builder()
                .name(userData.get(fieldNameForHeaderAttribute(HeaderAttribute.GIVEN_NAME)))
                .lastName(userData.get(fieldNameForHeaderAttribute(HeaderAttribute.SURNAME)))
                .affiliation(userData.get(fieldNameForHeaderAttribute(HeaderAttribute.AFFILIATION)))
                .homeOrg(userData.get(fieldNameForHeaderAttribute(HeaderAttribute.HOME_ORGANIZATION)))
                .mail(userData.get(fieldNameForHeaderAttribute(HeaderAttribute.MAIL)))
                .shibbolethSessionId(userData.get(fieldNameForHeaderAttribute(HeaderAttribute.SHIB_SESSION_ID)))
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

    private String fieldNameForHeaderAttribute(HeaderAttribute headerAttribute) {
        return switch (headerAttribute) {
            case SHIB_SESSION_ID -> "sessionId";
            case GIVEN_NAME -> "name";
            case SURNAME -> "lastName";
            case AFFILIATION -> "affiliation";
            case HOME_ORGANIZATION -> "homeOrg";
            case MAIL -> "mail";
        };
    }
}
