package account.model.records.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import static account.security.messages.AuthMessages.EMAIL_REGEX;

public record RecordRequestSignup(
                                  @NotEmpty
                                  String name,
                                  @NotEmpty
                                  String lastname,
                                  @NotNull
                                  @Pattern(regexp = EMAIL_REGEX)
                                  String email,
                                  @NotEmpty
                                  String password) {
}