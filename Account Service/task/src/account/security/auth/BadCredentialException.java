package account.security.auth;

import lombok.Getter;
import org.springframework.security.authentication.BadCredentialsException;
@Getter
public class BadCredentialException extends
                                         BadCredentialsException {
    private final String user;
    public BadCredentialException(String message,
                                  String user) {
        super(message);
        this.user = user;
    }
}
