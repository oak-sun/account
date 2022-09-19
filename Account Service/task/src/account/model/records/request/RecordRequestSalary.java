package account.model.records.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static account.security.messages.AccountMessages.PERIOD_REGEX;
import static account.security.messages.AuthMessages.*;

public record RecordRequestSalary(
                                  @NotNull
                                  @Pattern(regexp = EMAIL_REGEX,
                                           message = "Not a valid corporate Email")
                                  String employee,

                                  @NotNull
                                  @Pattern(regexp = PERIOD_REGEX,
                                           message = "Wrong date!")
                                  String period,
                                  @Min(value = 0,
                                       message = "Salary must be non negative!")
                                  long salary) {
}