package ch.zhaw.vorwahlen.security.authentication;

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
import java.util.EnumMap;
import java.util.Map;

import static ch.zhaw.vorwahlen.security.authentication.AuthFilter.HeaderAttribute.*;

/**
 * This class filters the request and response based on the {@link UsernamePasswordAuthenticationToken}.
 * If the Authentication does not exist it will be created with the user provided in the request.
 */
@RequiredArgsConstructor
@Setter
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private final boolean isProd;
    private final StudentRepository studentRepository;
    private Map<HeaderAttribute, String> testingUserData;

    public enum HeaderAttribute {
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var userData = new EnumMap<HeaderAttribute, String>(HeaderAttribute.class);
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (isProd && auth == null) {
            extractUserInfoFromHeader(request, userData);
        } else if (!isProd) {
            userData = new EnumMap<>(testingUserData);
        }

        if (isUserDataNotNull(userData) && (auth == null || !auth.isAuthenticated())) {
            var email = userData.get(HeaderAttribute.MAIL);
            boolean isStudentExistent = studentRepository.existsById(email);
            auth = new UsernamePasswordAuthenticationToken(createUser(userData, isStudentExistent), null);
            log.debug("received login request from {}", email);
            log.debug(userData.toString());
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private boolean isUserDataNotNull(Map<HeaderAttribute, String> userData) {
        if (userData.isEmpty()) {
            return false;
        }

        for (var s : userData.values()) {
            if (s == null || s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void extractUserInfoFromHeader(HttpServletRequest request, Map<HeaderAttribute, String> userData) {
        for (var headerAttribute: HeaderAttribute.values()) {
            var headerValue = request.getHeader(headerAttribute.name);
            if (headerValue != null) {
                userData.put(headerAttribute, URLDecoder.decode(headerValue, StandardCharsets.UTF_8));
            }
        }
    }

    private User createUser(Map<HeaderAttribute, String> userData, boolean isExistent) {
        return User.builder()
                .name(userData.get(GIVEN_NAME))
                .lastName(userData.get(SURNAME))
                .affiliation(userData.get(AFFILIATION))
                .homeOrg(userData.get(HOME_ORGANIZATION))
                .mail(userData.get(MAIL))
                .shibbolethSessionId(userData.get(SHIB_SESSION_ID))
                .role("USER")
                .isExistent(isExistent)
                .build();
    }
}
