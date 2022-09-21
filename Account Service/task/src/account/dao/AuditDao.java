package account.dao;

import account.model.Audit;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditDao  extends ReactiveSortingRepository<Audit, Long> {
}