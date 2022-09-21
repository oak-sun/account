package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AccessHandler implements ServerAccessDeniedHandler {
    private final Logger logger;

    @Autowired
    public AccessHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange,
                             AccessDeniedException denied) {
        return exchange
                .getPrincipal()
                .flatMap(user ->
                            logger.logAccessDenied(
                                            user.getName(),
                                            exchange
                                                 .getRequest()
                                                 .getPath()
                                                 .value()))

                .flatMap(secEvent -> Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        denied.getMessage() +
                                                "!")));
    }
}
