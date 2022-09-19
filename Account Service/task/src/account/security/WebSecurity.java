package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
public class WebSecurity {

    @Bean
    @Autowired
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveAuthenticationManager authManager) {
        http
                .csrf()
                .disable()
                .httpBasic(httpBasicSpec ->
                        httpBasicSpec
                            .authenticationManager(authManager)
                            .authenticationEntryPoint((exchange, ex) ->
                                    Mono.error(new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            ex.getMessage())))
                )
                .authorizeExchange()

                .pathMatchers("/api/auth/signup")
                .permitAll()

                .pathMatchers(HttpMethod.GET,
                        "/actuator",
                                  "/actuator/**")
                .permitAll()

                .pathMatchers("/error",
                                         "/error/**")
                .permitAll()

                .pathMatchers("/api/admin/**")
                .hasRole("ADMINISTRATOR")

                .pathMatchers("/api/acct/**")
                .hasRole("ACCOUNTANT")
                .pathMatchers(HttpMethod.GET,
                        "/api/empl/payment")
                .hasAnyRole("ACCOUNTANT",
                                   "USER")

                .pathMatchers("/api/**")
                .authenticated()

                .and()

                .exceptionHandling(exHand ->
                        exHand
                                .accessDeniedHandler((
                                        exchange, denied) -> Mono.error(
                                                new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                denied.getMessage() + "!")))
                )
                .formLogin();
        return http.build();
    }
}