package account.service;

import account.dao.UserDao;
import account.model.User;
import account.model.records.request.RecordRequestChangePass;
import account.model.records.request.RecordRequestSignup;
import account.model.records.response.RecordResponseChangePass;
import account.model.records.response.RecordResponseSignup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import java.security.Principal;
import static account.security.messages.AuthMessages.*;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Service
@Slf4j
public class AuthService {
    private final Validator validator;
    private final UserDao dao;
    private final PasswordEncoder encoder;

    @Autowired
    public AuthService(Validator validator,
                       UserDao dao,
                       PasswordEncoder encoder) {
        this.validator = validator;
        this.dao = dao;
        this.encoder = encoder;
    }

    public Mono<ServerResponse> signup(ServerRequest req) {
        return req
                .bodyToMono(RecordRequestSignup.class)
                .flatMap(r -> ok().body(
                        validateAndSave(r),
                        RecordResponseSignup.class));
    }

    public Mono<ServerResponse> changePassword(ServerRequest req) {
        return req
                .bodyToMono(RecordRequestChangePass.class)
                .zipWith(req.principal())
                .flatMap(tuple -> ok()
                        .body(validateAndChangepass(tuple),
                              RecordResponseChangePass.class));
    }

    private Mono<RecordResponseChangePass> validateAndChangepass(
                                      Tuple2<RecordRequestChangePass,
                                      ? extends Principal> tuple) {

        final var newPassword = tuple
                                       .getT1()
                                      .newPassword();
        var passwordValidationError =
                validatePassword(newPassword);
        if (!passwordValidationError.isEmpty()) {
            return Mono.error(
                         new ServerWebInputException(
                                 passwordValidationError));
        }
        return dao
                .findByEmail(tuple.getT2().getName())
                .ofType(User.class)
                .flatMap(user -> {
                    if (encoder
                            .matches(newPassword, user.getPassword())) {
                        return Mono.error(
                                new ServerWebInputException(
                                        SAME_PASSWORD_ERRORMSG));
                    } else {
                        user.setPassword(
                                         encoder.encode(newPassword));

                        return dao.save(user)
                                .map(u -> new RecordResponseChangePass(
                                        u.getEmail(),
                                        PASSWORD_UPDATEMSG));
                    }});
    }

    private Mono<RecordResponseSignup> validateAndSave(RecordRequestSignup record) {
        var errors =
                new BeanPropertyBindingResult(record,
                                              RecordRequestSignup.class
                                                .getName());
        validator.validate(record, errors);
        if (errors.hasErrors()) {
            return Mono.error(
                    new ServerWebInputException(errors
                                                   .getAllErrors()
                                                   .toString()));
        }
        var passwordValidationError =
                validatePassword(record.password());

        if (!passwordValidationError.isEmpty()) {
            return Mono.error(
                    new ServerWebInputException(
                            passwordValidationError));
        }
        return saveUser(record);
    }


    private String validatePassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH) {
            return PASSWORD_TOO_SHORT_ERRORMSG;
        }
        if (passwordIsHacked(password)) {
            return PASSWORD_HACKED_ERRORMSG;
        }
        return "";
    }

    private Mono<RecordResponseSignup> saveUser(RecordRequestSignup record) {
        return dao
                .findByEmail(record.email())
                .defaultIfEmpty(User.unknown())
                .ofType(User.class)
                .flatMap(user -> {
                    if (user.isUnknown()) {
                        return dao
                                .save(User
                                        .fromSignupRequest(record,
                                                           encoder
                                                                   .encode(record.password())))
                                .map(User::toSignupResponse);
                    } else {
                        return Mono.error(
                                new ServerWebInputException(USER_EXISTS_ERRORMSG));
                    }});
    }
}
