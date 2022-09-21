package account.dao;

import account.model.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

@Repository
public interface UserRoleDao extends ReactiveCrudRepository<UserRole, Long> {

    Flux<UserRole> findAllByEmail(String email);

    Mono<Void> deleteAllByEmail(String email);

    Mono<Void> deleteByEmailAndRole(String email, String role);

    default Mono<List<String>> findRolesByEmail(String email) {
        return findAllByEmail(email)
                .map(UserRole::getRole)
                .collectList();
    }
}
