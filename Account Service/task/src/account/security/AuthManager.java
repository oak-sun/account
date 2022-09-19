package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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

    @Autowired
    public AuthManager(ReactiveUserDetailsService service,
                       PasswordEncoder encoder) {
        super(service);
        setPasswordEncoder(encoder);
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (passwordIsHacked(
                (String) authentication.getCredentials())) {
            return Mono
                    .error(new BadCredentialsException(
                            PASSWORD_HACKED_ERRORMSG +
                                    " Please change!"));
        }
        return super.authenticate(authentication);
    }
}
