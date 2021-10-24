package ch.zhaw.vorwahlen.authentication;

import ch.zhaw.vorwahlen.model.user.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            try {
                User user = User.builder()
                        .name(request.getHeader("givenname"))
                        .lastName(request.getHeader("surname"))
                        .affiliation(request.getHeader("affiliation"))
                        .homeOrg(request.getHeader("homeorganization"))
                        .mail(request.getHeader("mail"))
                        .build();
                auth = new CustomAuthToken(request.getHeader("shib-session-id"), user);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (NullPointerException e) {
                System.err.println("Unable to parse field " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
