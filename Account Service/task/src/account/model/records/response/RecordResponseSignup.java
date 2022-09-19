package account.model.records.response;

import java.util.List;

public record RecordResponseSignup(long id,
                                   String name,
                                   String lastname,
                                   String email,
                                   List<String> roles) {
}