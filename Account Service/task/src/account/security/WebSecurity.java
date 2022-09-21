package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

@EnableWebFluxSecurity
public class WebSecurity {
    @Bean
    @Autowired
    public SecurityWebFilterChain springSecurityFilterChain(
                                                     ServerHttpSecurity http,
                                                     AuthManager manager,
                                            ServerAccessDeniedHandler handler,
                                   ServerAuthenticationEntryPoint entryPoint) {
        http
                .csrf()
                .disable()
                .httpBasic(h -> h
                                .authenticationManager(manager)
                                .authenticationEntryPoint(entryPoint))
                .authorizeExchange()
                .pathMatchers("/api/auth/signup")
                .permitAll()

                .pathMatchers(HttpMethod.GET,
                        "/actuator",
                                "/actuator/**")
                .permitAll()

                .pathMatchers("/error", "/error/**")
                .permitAll()

                .pathMatchers("/api/security/**")
                .hasRole("AUDITOR")

                .pathMatchers("/api/admin/**")
                .hasRole("ADMINISTRATOR")

                .pathMatchers("/api/acct/**")
                .hasRole("ACCOUNTANT")

                .pathMatchers(HttpMethod.GET,
                        "/api/empl/payment")
                .hasAnyRole("ACCOUNTANT", "USER")

                .pathMatchers("/api/**")
                .authenticated()

                .and()

                .exceptionHandling(ex -> ex.accessDeniedHandler(handler))
                .formLogin();
        return http.build();
    }
}