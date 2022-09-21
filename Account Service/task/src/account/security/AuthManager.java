package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static account.security.messages.AuthMessages.*;

@Component
public class AuthManager extends
                          UserDetailsRepositoryReactiveAuthenticationManager {
    private final Protector protector;

    @Autowired
    public AuthManager(ReactiveUserDetailsService service,
                       PasswordEncoder encoder,
                       Protector protector) {
        super(service);
        setPasswordEncoder(encoder);
        this.protector = protector;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (passwordIsHacked(
                (String) authentication.getCredentials())) {
            return Mono.error(
                    new BadCredentialException(
                            PASSWORD_HACKED_ERRORMSG + " Please change!",
                    authentication.getName()));
        }
        return super.authenticate(authentication)
                .doOnSuccess(auth -> protector
                                           .resetUserFailures(auth.getName()))
                .onErrorMap(ex ->
                        new BadCredentialException(
                                ex.getMessage(),
                                authentication.getName()));
    }
}
