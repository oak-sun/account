package account.model.records.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import static account.security.messages.AuthMessages.EMAIL_REGEX;

public record  RecordRequestRole(@NotNull
                                 @Pattern(regexp = EMAIL_REGEX,
                                          message = "Not a valid corporate Email")
                                 String user,
                                 @NotEmpty String role,
                                 @NotNull
                                 @Pattern(regexp =
                                         "(?i)grant|remove",
                                         message = "operation needs 'grant' or 'remove'")
                                 String operation) {
}