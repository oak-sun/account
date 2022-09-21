package account.security.auth;

import account.dao.AuditDao;
import account.model.Audit;
import account.model.records.request.RecordRequestRole;
import account.model.records.request.RecordRequestUserLock;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class Logger {

    private final AuditDao dao;

    public Mono<Audit> logCreateUser(String newUser) {
        return dao.save(Audit
                               .builder()
                               .action("CREATE_USER")
                        .subject("Anonymous")
                        .object(newUser)
                        .path("/api/auth/signup")
                        .build());
    }

    public Mono<Audit> logToggleRole(String admin,
                                     RecordRequestRole record) {
        var action = record
                            .operation()
                            .toUpperCase() +
                            "_ROLE";
        var role = record
                                .role()
                                .toUpperCase();
        var object = record
                                 .operation()
                                 .equalsIgnoreCase("GRANT")
                ?
                "Grant role %s to %s"
                                    .formatted(role, record.user())
                :
                "Remove role %s from %s"
                                       .formatted(role, record.user());
        return dao.save(Audit
                               .builder()
                               .action(action)
                               .subject(admin)
                               .object(object)
                               .path("/api/admin/user/role")
                               .build());
    }

    public Mono<Audit> logToggleUserLock(String admin,
                                         RecordRequestUserLock record) {
        var action = record
                          .operation()
                          .toUpperCase() +
                          "_USER";
        var object = record
                                 .operation()
                                 .equalsIgnoreCase("LOCK")
                ?
                "Lock user %s"
                             .formatted(record.user())
                : "Unlock user %s"
                                 .formatted(record.user());
        return dao.save(Audit
                              .builder()
                              .action(action)
                              .subject(admin)
                              .object(object)
                              .path("/api/admin/user/access")
                              .build());
    }

    public Mono<Audit> logDeleteUser(String admin, String user) {
        return dao.save(Audit
                            .builder()
                            .action("DELETE_USER")
                            .subject(admin)
                            .object(user)
                            .path("/api/admin/user")
                .build());
    }

    public Mono<Audit> logChangePassword(String email) {
        return dao.save(Audit
                            .builder()
                            .action("CHANGE_PASSWORD")
                            .subject(email)
                            .object(email)
                            .path("/api/auth/changepass")
                            .build());
    }

    public Mono<Audit> logAccessDenied(String user, String path) {
        return dao.save(Audit
                            .builder()
                            .action("ACCESS_DENIED")
                            .subject(user)
                            .object(path)
                            .path(path)
                            .build());
    }

    public Mono<Audit> logFailedUser(String user, String path) {
        return dao.save(Audit
                            .builder()
                            .action("LOGIN_FAILED")
                            .subject(user)
                            .object(path)
                            .path(path)
                            .build());
    }

    public Mono<Audit> logBruteForce(String user, String path) {
        return dao.save(Audit
                             .builder()
                             .action("BRUTE_FORCE")
                             .subject(user)
                             .object(path)
                            .path(path)
                            .build())
                .then(
                        dao.save(Audit
                                      .builder()
                                      .action("LOCK_USER")
                                      .subject(user)
                                      .object("Lock user %s"
                                                          .formatted(user))
                                      .path(path)
                                      .build()));
    }
}