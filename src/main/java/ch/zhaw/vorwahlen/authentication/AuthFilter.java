package ch.zhaw.vorwahlen.authentication;

import ch.zhaw.vorwahlen.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Setter
public class AuthFilter extends OncePerRequestFilter {
    Map<String, String> userData = new HashMap<>();
    private final boolean isProd;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (isProd) {
            extractUserInfoFromHeader(request);
        }

        if (isUserDataNotNull() && (auth == null || !auth.isAuthenticated())) {
            auth = new CustomAuthToken(userData.get("sessionId"), createUser());
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private boolean isUserDataNotNull() {
        for (String s : userData.values()) {
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
    }

    private User createUser() {
        return User.builder()
                .name(userData.get("name"))
                .lastName(userData.get("lastName"))
                .affiliation(userData.get("affiliation"))
                .homeOrg(userData.get("homeOrg"))
                .mail(userData.get("mail"))
                .role(userData.get("role"))
                .build();
    }
}
