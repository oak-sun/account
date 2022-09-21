package account.service;

import account.dao.SalaryDao;
import account.dao.UserDao;
import account.dao.UserRoleDao;
import account.model.Role;
import account.model.User;
import account.model.UserRole;
import account.model.records.request.RecordRequestRole;
import account.model.records.request.RecordRequestUserLock;
import account.model.records.response.RecordResponseSignup;
import account.model.records.response.RecordResponseStatus;
import account.model.records.response.RecordResponseUserDeleted;
import account.security.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static account.security.messages.AdminMessages.*;
import static account.security.messages.AuthMessages.EMAIL_REGEX;
import static java.lang.Boolean.TRUE;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Service
public class AdminService {
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final SalaryDao salaryDao;
    private final Logger logger;
    private final List<Role> systemRoles;
    private final Validator validator;

    @Autowired
    public AdminService(UserDao userDao,
                        UserRoleDao userRoleDao,
                        SalaryDao salaryDao,
                        Logger logger,
                        List<Role> systemRoles,
                        Validator validator) {

        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.salaryDao = salaryDao;
        this.logger = logger;
        this.systemRoles = systemRoles;
        this.validator = validator;
    }

    public Mono<ServerResponse> displayUsers(ServerRequest ignoredServerRequest) {
        return ok()
                .body(userDao
                        .findAll(Sort.by(
                                Sort.Direction.ASC, "id"))
                .flatMap(user -> Mono
                                .just(user)
                                .zipWith(
                                        userRoleDao.findRolesByEmail(
                                                        user.getEmail()),
                                                         User::setRoles))
                .map(User::toSignupResponse),
                        RecordResponseSignup.class);
    }
    public Mono<ServerResponse> deleteUser(ServerRequest req) {
        var email = req.pathVariable("email");
        if (!email.matches(EMAIL_REGEX)) {
            return Mono.error(
                    new ServerWebInputException(
                            "Invalid user email given: '" +
                                    email + "'!"));
        }
        return ok().body
                (deleteUser(email, req.principal()),
                        RecordResponseUserDeleted.class);
    }

    private Mono<RecordResponseUserDeleted> deleteUser(
                                             String email,
                                 Mono<? extends Principal>
                                              principal) {
        return userRoleDao
                       .findRolesByEmail(email)
                       .flatMap(this::isAdmin)
                       .flatMap(isAdmin -> {

                    if (TRUE.equals(isAdmin)) {
                        return Mono
                                .error(
                                        new ServerWebInputException(
                                        CANT_DELETE_ADMIN_ERRORMSG));
                    } else {
                        return userRoleDao
                                .deleteAllByEmail(email)
                                .then(salaryDao
                                        .deleteAllByEmail(
                                                email))
                                .then(userDao
                                        .deleteByEmail(email))
                                .then(principal)
                                .flatMap(admin -> logger
                                                .logDeleteUser(
                                                        admin.getName(), email))
                                .map(secEvent ->
                                        new RecordResponseUserDeleted(email,
                                                DELETED_SUCCESSFULLY));
                    }
                });
    }
    private Mono<Boolean> isAdmin(List<String> roles) {
        if (roles.isEmpty()) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            USER_NOT_FOUND_ERRORMSG));
        }
        return Mono.just(roles.contains(ADMIN_ROLE));
    }

    public Mono<ServerResponse> toggleRole(ServerRequest req) {
        return req
                .bodyToMono(RecordRequestRole.class)
                .flatMap(r ->
                        ok()
                        .body(validateAndToggleRole(r, req.principal()),
                        RecordResponseSignup.class));
    }

    public Mono<ServerResponse> toggleUserLock(ServerRequest req) {
        return req
                .bodyToMono(RecordRequestUserLock.class)
                .flatMap(r ->
                        ok().body(
                                validateAndToggleLock(r,
                                        req.principal()),
                                RecordResponseStatus.class));
    }

    private Mono<RecordResponseStatus> validateAndToggleLock(
                                                     RecordRequestUserLock record,
                                                     Mono<? extends Principal>
                                                             principal) {

        var hibernateValidationErrors =
                validateHibernate(record, RecordRequestUserLock.class);

        if (!hibernateValidationErrors
                .isEmpty()) {
            return Mono.error(
                    new ServerWebInputException(
                            hibernateValidationErrors));
        }
        var lockRequested = record
                                         .operation()
                                         .equalsIgnoreCase("lock");
        return userRoleDao

                .findRolesByEmail(record.user())
                .flatMap(this::isAdmin)
                .flatMap(isAdmin -> {

                    if (TRUE.equals(isAdmin)) {
                        return Mono.error(
                                new ServerWebInputException(
                                        CANT_LOCK_ADMIN_ERRORMSG));
                    } else {
                        return userDao
                                     .toggleLock(record.user(),
                                                 lockRequested)
                                     .then(principal)
                                .flatMap(admin -> logger
                                                 .logToggleUserLock(admin.getName(),
                                                                    record))
                                .map(secEvent -> new RecordResponseStatus(
                                        "User %s %sed!"
                                                .formatted(record.user(),
                                                           lockRequested
                                                                   ?
                                                                   "lock"
                                                                   :
                                                                   "unlock")));
                    }
                });
    }
    private Mono<RecordResponseSignup> validateAndToggleRole(
                                                             RecordRequestRole record,
                                                             Mono<? extends Principal>
                                                                           principal) {
        var hibernateValidationErrors =
                validateHibernate(record, RecordRequestRole.class);

        if (!hibernateValidationErrors
                .isEmpty()) {

            return Mono.error(
                    new ServerWebInputException(hibernateValidationErrors));
        }
        if (systemRoles
                      .stream()
                      .map(Role::getRoleName)
                      .noneMatch(role ->
                             role.endsWith(
                                     record.role().toUpperCase()))) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            ROLE_NOT_FOUND_ERRORMSG));
        }
        return userRoleDao
                         .findRolesByEmail(record.user())
                         .flatMap(userRoles ->
                                 isOperationValid(userRoles, record))
                         .flatMap(reqRole -> record
                                             .operation()
                                             .equalsIgnoreCase("remove")
                                             ?
                                 userRoleDao.deleteByEmailAndRole(
                                                       record.user(),
                                                        reqRole)
                                             :
                                 userRoleDao.save(
                                                  UserRole.builder().email(
                                                               record
                                                                 .user())
                                                                 .role(reqRole)
                                                                 .build()))
                         .then(principal)
                         .flatMap(admin -> logger
                                                 .logToggleRole(admin.getName(),
                                                   record))
                         .flatMap(sec -> updatedUserResponse(
                                 record.user()));
    }

    private Mono<RecordResponseSignup> updatedUserResponse(String email) {
        return userDao
                .findByEmail(email)
                .flatMap(user -> Mono
                                    .just(user)
                                    .zipWith(userRoleDao
                                                  .findRolesByEmail(
                                                          user.getEmail()),
                                                          User::setRoles))
                .map(User::toSignupResponse);
    }

    private Mono<String> isOperationValid(List<String> userRoles,
                                          RecordRequestRole record) {
        if (userRoles.isEmpty()) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            USER_NOT_FOUND_ERRORMSG));
        }
        var isRemove = record
                                     .operation()
                                     .equalsIgnoreCase("remove");

        var reqRole = "ROLE_" + record
                                            .role()
                                            .toUpperCase();
        if (isRemove &&
                       !userRoles
                                .contains(reqRole)) {
            return Mono.error(
                    new ServerWebInputException(
                            USER_HASNT_ROLE_ERRORMSG));
        }
        if (isRemove && userRoles.size() == 1) {
            return Mono.error(
                       new ServerWebInputException(
                               reqRole.equals(ADMIN_ROLE)
                                       ?
                                       CANT_DELETE_ADMIN_ERRORMSG
                                       :
                                       USER_NEEDS_ROLE_ERRORMSG));
        }
        if (!isRemove &&
                         userRoles.contains(reqRole)) {
            return Mono.error(
                    new ServerWebInputException(
                            USER_HAS_ROLE_ALREADY_ERRORMSG));
        }
        if (!isRemove
                &&
                (reqRole.equals(ADMIN_ROLE)
                        ||
                        userRoles.contains(ADMIN_ROLE))) {
            return Mono.error(
                    new ServerWebInputException(
                            INVALID_ROLE_COMBINE_ERRORMSG));
        }
        return Mono.just(reqRole);
    }

    private <T> String validateHibernate(T request, Class<T> classOfRequest) {

        var errors = new BeanPropertyBindingResult(
                              request,
                             classOfRequest.getName());

        validator.validate(request, errors);
        return errors.hasErrors()
                ?
                errors
                        .getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::
                                            getDefaultMessage)
                        .collect(Collectors.joining(" && "))
                :
                "";
    }
}
