package account.service;

import account.dao.AuditDao;
import account.model.Audit;
import account.model.records.response.RecordResponseAudit;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Service
@AllArgsConstructor
public class AuditService {
    private final AuditDao dao;
    public Mono<ServerResponse> getAuditEvents(ServerRequest ignoredReq) {
        return ok()
                .body(dao.findAll(
                           Sort.by(Sort.Direction.ASC, "id"))
                .map(Audit::toResponse),
                        RecordResponseAudit.class);
    }
}
