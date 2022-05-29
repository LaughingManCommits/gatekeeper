package you.shall.not.pass.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import you.shall.not.pass.repositories.UserRepository;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigAdapter extends WebSecurityConfigurerAdapter {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionCookieSecurityContextRepository sessionCookieSecurityContextRepository;
    private final CustomBasicAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder)
            throws Exception {
        authenticationManagerBuilder
                .userDetailsService(new CustomUserDetailService(userRepository))
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().securityContext().securityContextRepository(sessionCookieSecurityContextRepository)
                .and().csrf().disable()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .authorizeRequests()
                .antMatchers("/authenticate")
                .permitAll();
    }

}
