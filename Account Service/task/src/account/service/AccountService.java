package account.service;


import account.dao.SalaryDao;
import account.dao.UserDao;
import account.model.Salary;
import account.model.User;
import account.model.records.request.RecordRequestSalary;
import account.model.records.response.RecordResponseSalary;
import account.model.records.response.RecordResponseStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static account.security.messages.AccountMessages.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.function.Predicate.not;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Service
@Slf4j
@AllArgsConstructor
public class AccountService {
    private final UserDao daoU;
    private final SalaryDao daoS;
    private final Validator validator;

    public Mono<ServerResponse> accessPayrolls(ServerRequest request) {

        Optional<String> searchPeriod = request
                .queryParam("period");

        if (searchPeriod.isPresent() &&
                !searchPeriod
                        .get()
                        .matches("(0[1-9]|1[0-2])-[1-9]\\d{3}")) {
            return Mono.error(
                    new ServerWebInputException(
                            "Wrong Date: Use mm-yyyy format!"));
        }
        return request
                .principal()
                .flatMap(principal -> ok()
                                          .body(selectSalaries(principal.getName(),
                                                             searchPeriod),
                        new ParameterizedTypeReference<>(){}));
    }

    private Mono<List<RecordResponseSalary>> selectSalaries(String email,
                                                            Optional<String> searchPeriod) {
        return daoU
                .findByEmail(email)
                .ofType(User.class)
                .flatMap(user -> searchPeriod.isEmpty()
                        ?
                        daoS.findAllByEmail(email,
                                           Sort.by(ASC, "period"))
                        .map(salary -> RecordResponseSalary
                                                          .fromUserAndSalary(salary, user))
                        .collectList()
                        :
                        daoS
                        .findByEmployeeAndPeriod(email, Salary.yearFirst(searchPeriod.get()))
                        .map(salary -> List.of(
                                       RecordResponseSalary.fromUserAndSalary(salary, user))));
    }

    public Mono<ServerResponse> changePayrolls(ServerRequest request) {
        return request
                .bodyToMono(RecordRequestSalary.class)
                .flatMap(rec -> ok().body(validateAndUpdate(rec),
                                          RecordResponseStatus.class));
    }

    private Mono<RecordResponseStatus> validateAndUpdate(RecordRequestSalary record) {
        var hibernateValidationErrors = validateHibernate(record);
        if (!hibernateValidationErrors.isEmpty()) {
            return Mono.error(
                    new ServerWebInputException(hibernateValidationErrors));
        }
        return daoS
                .findByEmployeeAndPeriod(record.employee(),
                                         Salary.yearFirst(record.period()))
                .defaultIfEmpty(Salary.empty())
                .flatMap(salary -> salary.isEmpty()
                        ?
                        Mono.error(
                                new ServerWebInputException(NO_SUCH_SALES_RECORD_ERRORMSG))
                        :
                        daoS
                                .save(salary
                                        .setMonthlySalary(record.salary()))
                                .map(saved -> new RecordResponseStatus(UPDATED_SUCCESSFULLY)));
    }

    @Transactional
    public Mono<ServerResponse> uploadPayrolls(ServerRequest req) {
        return req
                .bodyToFlux(RecordRequestSalary.class)
                .index()
                .flatMap(this::validateAll)
                .collectList()
                .flatMap(list -> ok().body(
                                        saveSalaryRecord(list),
                                        RecordResponseStatus.class));
    }

    private Mono<Tuple2<RecordRequestSalary, String>> validateAll(Tuple2<Long,
                                                                  RecordRequestSalary> tuple) {

        var hibernateValidationErrors = validateHibernate(tuple.getT2());
        Mono<String> err = Mono.just(hibernateValidationErrors.isEmpty()
                ?
                ""
                :
                RECORDMSG_START.formatted(tuple.getT1(),
                                          hibernateValidationErrors));
        if (hibernateValidationErrors.isEmpty()) {
            err = validateWithDatabase(tuple.getT1(), tuple.getT2());
        }
        return Mono
                .just(tuple.getT2())
                .zipWith(err);
    }

    private String validateHibernate(RecordRequestSalary record) {

        var errors = new BeanPropertyBindingResult(
                record, RecordRequestSalary.class.getName());
        validator.validate(record, errors);
        return errors.hasErrors()
                ?
                errors
                        .getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable
                                ::getDefaultMessage)
                        .collect(Collectors.joining(" && "))
                :
                "";
    }

    private Mono<String> validateWithDatabase(long recordId,
                                              RecordRequestSalary record) {
        return daoU
                .findByEmail(record.employee())
                .hasElement()
                .flatMap(hasUserElement -> {

                    if (FALSE.equals(hasUserElement)) {
                        return Mono.just(
                                RECORDMSG_START.formatted(recordId,
                                        NO_SUCH_EMPLOYEE_ERRORMSG));
                    } else {
                        return daoS
                                .findByEmployeeAndPeriod(record.employee(),
                                        Salary.yearFirst(record.period()))
                                .hasElement()
                                .map(hasSalaryElement ->
                                        TRUE.equals(hasSalaryElement)
                                        ?
                                        RECORDMSG_START
                                                .formatted(recordId,
                                                        RECORD_ALREADY_EXISTS_ERRORMSG)
                                        :
                                        "");
                    }});
    }

    private Mono<RecordResponseStatus>
                   saveSalaryRecord(List<Tuple2<RecordRequestSalary,
                                    String>> tuples) {
        if (tuples
                .stream()
                .anyMatch(not(
                               tuple -> tuple
                                           .getT2()
                                           .isEmpty()))) {
            var joinedErrorMessage =
                    tuples
                            .stream()
                            .map(Tuple2::getT2)
                            .filter(not(String::isEmpty))
                            .collect(Collectors
                                    .joining(" | "));
            return Mono.error(
                    new ServerWebInputException(joinedErrorMessage));
        }
        if (tuples
                .stream()
                .map(Tuple2::getT1)
                .collect(Collectors.groupingBy(rec ->
                        rec
                                .employee()
                                .toLowerCase() + rec.period()))
                .values()
                .stream()
                .map(List::size)
                .anyMatch(s -> s > 1)) {

            return Mono.error(
                    new ServerWebInputException(
                            DUPLICATE_RECORDS_ERRORMSG));
        }
        return daoS
                   .saveAll(tuples
                               .stream()
                               .map(Tuple2::getT1)
                               .map(Salary::fromSalaryRecord)
                               .toList())
                   .count()
                   .map(count ->
                           new RecordResponseStatus("%d records %s"
                                   .formatted(count, ADDED_SUCCESSFULLY)));
    }
}