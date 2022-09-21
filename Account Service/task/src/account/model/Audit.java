package account.model;

import account.model.records.response.RecordResponseAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

/**
 * Entity class connected to R2DBC-Table AUDIT, that stores employee salary record to a month period.
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("AUDIT")
public class Audit {
    @Id
    private long id;
    @Builder.Default()
    private LocalDate date = LocalDate.now();

    private String action;
    private String subject;
    private String object;
    private String path;

    public RecordResponseAudit toResponse() {
        return new RecordResponseAudit(id,
                                       date,
                                       action,
                                       subject,
                                       object,
                                       path);
    }
}
