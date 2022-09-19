package account.service;

import account.dao.SalaryDao;
import account.dao.UserDao;
import account.dao.UserRoleDao;
import account.model.Role;
import account.model.User;
import account.model.UserRole;
import account.model.records.request.RecordRequestRole;
import account.model.records.response.RecordResponseSignup;
import account.model.records.response.RecordResponseUserDeleted;
import lombok.AllArgsConstructor;
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

import java.util.List;
import java.util.stream.Collectors;
import static account.security.messages.AuthMessages.*;
import static account.security.messages.AdminMessages.*;
import static java.lang.Boolean.TRUE;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Service
@AllArgsConstructor
public class AdminService {
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final SalaryDao salaryDao;
    private final List<Role> roles;
    private final Validator validator;

    public Mono<ServerResponse> displayUsers(ServerRequest ignoredServerRequest) {
        return ok()
                .body(userDao.findAll(Sort.by(
                        Sort.Direction.ASC, "id"))
                .flatMap(user ->
                        Mono.just(user).zipWith(
                                userRoleDao.findRolesByEmail(
                                        user.getEmail()),
                                User::setRoles))
                .map(User::toSignupResponse),
                        RecordResponseSignup.class);
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String email = request.pathVariable("email");
        if (!email.matches(EMAIL_REGEX)) {
            return Mono.error(
                    new ServerWebInputException("Invalid user email given: '" + email + "'!"));
        }
        return ok()
                .body(deleteUser(email),
                        RecordResponseUserDeleted.class);
    }

    private Mono<RecordResponseUserDeleted> deleteUser(String email) {
        return userRoleDao
                .findRolesByEmail(email)
                .flatMap(this::isAdmin)
                .flatMap(isAdmin -> {
                    if (TRUE.equals(isAdmin)) {
                        return Mono
                                .error(new ServerWebInputException(
                                        CANT_DELETE_ADMIN_ERRORMSG));
                    } else {
                        return userRoleDao
                                .deleteAllByEmail(email)
                                .then(salaryDao.deleteAllByEmail(email))
                                .then(userDao.deleteByEmail(email))
                                .then(Mono.just(
                                        new RecordResponseUserDeleted(email, DELETED_SUCCESSFULLY)));
                    }
                });
    }

    private Mono<Boolean> isAdmin(List<String> roles) {
        if (roles.isEmpty()) {
            return Mono
                    .error(new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            USER_NOT_FOUND_ERRORMSG));
        }
        return Mono.just(roles.contains(ADMIN_ROLE));
    }

    public Mono<ServerResponse> toggleRole(ServerRequest request) {
        return request
                .bodyToMono(RecordRequestRole.class)
                .flatMap(req -> ok()
                        .body(validateAndToggleRole(req),
                                RecordResponseSignup.class));
    }

    private Mono<RecordResponseSignup> validateAndToggleRole(RecordRequestRole record) {
        var hibernateValidationErrors = validateHibernate(record);
        if (!hibernateValidationErrors.isEmpty()) {
            return Mono.error(
                    new ServerWebInputException(hibernateValidationErrors));
        }
        if (roles
                .stream()
                .map(Role::getRoleName)
                .noneMatch(role ->
                        role.endsWith(record.role().toUpperCase()))) {
            return Mono.error(
                    new ResponseStatusException(HttpStatus.NOT_FOUND,
                            ROLE_NOT_FOUND_ERRORMSG));
        }
        return userRoleDao
                .findRolesByEmail(record.user())
                .flatMap(userRoles -> isOperationValid(userRoles,
                        record))
                .flatMap(requestedRole ->
                        record
                        .operation()
                        .equalsIgnoreCase("remove")
                        ?
                        userRoleDao.deleteByEmailAndRole(
                                record.user(), requestedRole)
                        .then(updatedUserResponse(record.user()))
                        :
                        userRoleDao.save(UserRole
                                        .builder()
                                        .email(record.user())
                                        .role(requestedRole)
                                        .build())
                        .then(updatedUserResponse(record.user()))
                );
    }

    private Mono<RecordResponseSignup> updatedUserResponse(String email) {
        return userDao
                .findByEmail(email)
                .flatMap(login -> Mono
                        .just(login)
                        .zipWith(userRoleDao
                                .findRolesByEmail(login.getEmail()),
                                User::setRoles))
                .map(User::toSignupResponse);
    }

    private Mono<String> isOperationValid(List<String> userRoles,
                                          RecordRequestRole record) {
        if (userRoles.isEmpty()) {
            return Mono.error(
                    new ResponseStatusException(HttpStatus.NOT_FOUND,
                            USER_NOT_FOUND_ERRORMSG));
        }

        var isRemove = record
                .operation()
                .equalsIgnoreCase("remove");
        var requestedRole = "ROLE_" +
                record.role().toUpperCase();

        if (isRemove &&
                !userRoles.contains(requestedRole)) {
            return Mono.error(
                    new ServerWebInputException(USER_HASNT_ROLE_ERRORMSG));
        }
        if (isRemove &&
                userRoles.size() == 1) {
            return Mono.error(
                    new ServerWebInputException(requestedRole.equals(ADMIN_ROLE)
                    ?
                            CANT_DELETE_ADMIN_ERRORMSG
                            :
                            USER_NEEDS_ROLE_ERRORMSG));
        }
        if (!isRemove &&
                userRoles.contains(requestedRole)) {
            return Mono.error(
                    new ServerWebInputException(USER_HAS_ROLE_ALREADY_ERRORMSG));
        }
        if (!isRemove &&
                (requestedRole.equals(ADMIN_ROLE) ||
                        userRoles.contains(ADMIN_ROLE))) {
            return Mono.error(
                    new ServerWebInputException(INVALID_ROLE_COMBINE_ERRORMSG));
        }
        return Mono.just(requestedRole);
    }

    private String validateHibernate(RecordRequestRole record) {
        var errors = new BeanPropertyBindingResult(record,
                RecordRequestRole.class.getName());

        validator.validate(record, errors);
        return errors
                .hasErrors()
                ?
                errors
                        .getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" && "))
                :
                "";
    }
}
