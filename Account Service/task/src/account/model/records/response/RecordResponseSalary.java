package account.model.records.response;

import account.model.Salary;
import account.model.User;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public record RecordResponseSalary(String name,
                                   String lastname,
                                   String period,
                                   String salary) {

    public static RecordResponseSalary fromUserAndSalary(Salary salary,
                                                         User user) {

        return new RecordResponseSalary(
                user.getName(),
                user.getLastname(),
                monthFirst(salary.getPeriod()),
                getSalaryText(salary.getMonthlySalary()));
    }

    private static String getSalaryText(long salary) {
        return "%d dollar(s) %02d cent(s)"
                .formatted(salary / 100, salary % 100);
    }

    private static String monthFirst(String period) {
        return Month.of(
                Integer
                        .parseInt(period.substring(5)))
                        .getDisplayName(TextStyle.FULL, Locale.US)
                +
                "-"
                +
                period.substring(0, 4);
    }
}