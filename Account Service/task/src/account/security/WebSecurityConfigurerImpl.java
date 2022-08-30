package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {
    @Autowired
    RestAuthenticationEntryPoint raep;
    @Autowired
    UserDetailServiceImpl udsimpl;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(this.udsimpl)
                .passwordEncoder(getEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic()
                .authenticationEntryPoint(raep) // Handle auth error
                .and()
                      .csrf()
                      .disable()
                      .headers()
                      .frameOptions()
                      .disable() // for Postman, the H2 console
                .and()
                      .authorizeRequests() // manage access

                      .mvcMatchers( "/api/auth/signup")
                      .permitAll()

                      .mvcMatchers(HttpMethod.POST, "/actuator/shutdown")
                      .permitAll()

                      .mvcMatchers("/**")
                      .authenticated() // or .anyRequest().authenticated()
                // other matchers
                .and()
                      .sessionManagement()
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}