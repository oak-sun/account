package account.security;

import account.dao.UserDao;
import account.model.Audit;
import account.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static account.security.messages.AdminMessages.LOGIN_FAILED_LIMIT;

@Component
@AllArgsConstructor
public class Protector {

    private final Logger logger;
    private final UserDao dao;

    public Mono<Audit> handleUserFail(String email, String path) {
        return dao
                .findByEmail(email)
                .defaultIfEmpty(User.unknown())
                .flatMap(user ->
                        checkFailedAttemptsAndHandle(user, path));
    }

    public void resetUserFailures(String email) {
        dao
                .findByEmail(email)
                .flatMap(user -> dao.save(
                                    user.setFailedLogins(0)))
                .subscribe();
    }

    private Mono<Audit> checkFailedAttemptsAndHandle(User user,
                                                     String path) {
        if (user.isUnknown() ||
                user.isAccountLocked()) {
            return logger
                    .logFailedUser(user.getEmail(),
                                    path);
        }
        user.setFailedLogins(
                user.getFailedLogins() + 1);
        if (user.getFailedLogins() < LOGIN_FAILED_LIMIT) {
            return dao
                    .save(user)
                    .then(logger.logFailedUser(
                            user.getEmail(), path));
        }
        return dao
                .toggleLock(user.getEmail(), true)
                .then(logger.logFailedUser(
                             user.getEmail(), path))
                .then(logger.logBruteForce(
                             user.getEmail(), path));
    }
}
