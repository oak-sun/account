package account.dao;

import account.model.User;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDao extends ReactiveSortingRepository<User, Long> {

    Mono<User> findByEmail(String email);
    Mono<Void> deleteByEmail(String email);

    default Mono<User> toggleLock(String email,
                                  boolean lockRequested) {
        return findByEmail(email)
                .map(user -> user
                                .setAccountLocked(lockRequested)
                                .setFailedLogins(0))
                .flatMap(this::save);
    }
}