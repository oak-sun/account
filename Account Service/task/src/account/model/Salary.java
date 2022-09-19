package account.model;

import account.model.records.request.RecordRequestSalary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("SALARY")
public class Salary {
    private static final Salary EMPTY = new Salary();

    @Id
    private long id;

    private String email;

    private String period;

    @Column("salary")
    private long monthlySalary;

    public static Salary fromSalaryRecord(RecordRequestSalary record) {
        return Salary
                     .builder()
                     .email(record.employee())
                     .monthlySalary(record.salary())
                     .period(yearFirst(record.period()))
                     .build();
    }

    public static String yearFirst(String period) {
        return period.substring(3) +
                "-" +
                period.substring(0,2);
    }

    public static Salary empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }
}