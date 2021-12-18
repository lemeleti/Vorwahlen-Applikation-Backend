package ch.zhaw.vorwahlen.security.authentication;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configure the permissions and filter the incoming requests.
 */
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityAdapter extends WebSecurityConfigurerAdapter {
    private final AuthFilter authFilter;
    private final CustomAuthProvider customAuthProvider;
    private final String[] allowedPaths = {"/texts/**", "/modules/**", "/", "/error**", "/session/is-authenticated", "/session/is-admin"};
    private final String[] protectedPaths = {"/texts/**", "/modules/**", "/students/**", "/elections/**"};
    private final String[] userProtectedPaths = { "/students/{student}/**", "/elections/{student}/structure/**" };
    @Value("${logoutPath}")
    private String logoutPath;
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(customAuthProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, allowedPaths).permitAll()
                .antMatchers(userProtectedPaths).access("@userSecurity.hasUserId(authentication, #student)")
                .antMatchers(protectedPaths).hasAuthority(CustomAuthProvider.ADMIN_ROLE)
                .anyRequest().authenticated()
                .and().httpBasic().disable()
                .addFilterBefore(authFilter, BasicAuthenticationFilter.class)
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", HttpMethod.GET.toString()))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl(logoutPath)
                .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}