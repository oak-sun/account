package account.model.records.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import static account.security.messages.AuthMessages.EMAIL_REGEX;

public record RecordRequestUserLock(
                                    @NotNull
                                    @Pattern(regexp = EMAIL_REGEX,
                                             message = "Not a valid corporate Email")
                                    String user,

                                    @NotNull
                                    @Pattern(regexp = "(?i)(un)*lock",
                                             message = "operation must be 'lock' or 'unlock'")
                                    String operation) {
}