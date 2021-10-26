package ch.zhaw.vorwahlen.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityAdapter extends WebSecurityConfigurerAdapter {
    private final AuthFilter authFilter;
    private final CustomAuthProvider customAuthProvider;
    private final String[] allowedPaths = {"/module**", "/", "/error**", "/session/is-authenticated", "/session/is-admin"};
    private final String[] protectedPaths = {"/module**", "/dispensation**", "/class**"};

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(customAuthProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, allowedPaths).permitAll()
                .antMatchers(protectedPaths).hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().httpBasic().disable()
                .addFilterBefore(authFilter, BasicAuthenticationFilter.class)
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}