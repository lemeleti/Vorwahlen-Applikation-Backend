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
    public static final String INIT_SESSION_URL = "https://vorwahlen.cloudlab.zhaw.ch/session/init";
    private final CustomAuthProvider customAuthProvider;
    private final CustomBasicAuthenticationEntryPoint authenticationEntryPoint;
    private final String[] allowedPaths = {"/module**", "/", "/error**"};
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
                .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint)
                .and().addFilterBefore(new AuthFilter(), BasicAuthenticationFilter.class)
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}