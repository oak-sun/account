package account.model.records.response;

import java.time.LocalDate;

public record RecordResponseAudit(long id,
                                  LocalDate date,
                                  String action,
                                  String subject,
                                  String object,
                                  String path) {
}