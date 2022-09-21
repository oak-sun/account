package account.security.auth;

import account.security.Protector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ServerAuthEntryPoint implements
                                  ServerAuthenticationEntryPoint {
    private final Protector protector;
    @Autowired
    public ServerAuthEntryPoint(Protector protector) {
        this.protector = protector;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
                               AuthenticationException ex) {

        if (ex instanceof BadCredentialException bad) {
            return protector
                    .handleUserFail(bad.getUser(),
                            exchange
                                    .getRequest()
                                    .getPath().value())
                    .then(Mono.error(
                            new ResponseStatusException(
                                    HttpStatus.UNAUTHORIZED,
                                    ex.getMessage())));
        }
        return Mono.error(
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        ex.getMessage()));
    }
}
