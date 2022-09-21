package account.dao;

import account.model.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends ReactiveCrudRepository<Role, Long> {
}