package account.model.records.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotEmpty;

public record RecordRequestChangePass(
                                      @NotEmpty
                                      @JsonProperty("new_password")
                                      String newPassword) {
}