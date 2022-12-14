package account.security.messages;

import account.dao.UserDao;
import account.dao.UserRoleDao;
import account.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
public class AuthMessages {

    @Bean
    @Autowired
    public ReactiveUserDetailsService
                                     userDetailsService(UserDao userDao,
                                                        UserRoleDao userRoleDao) {
        return email ->
                userDao
                .findByEmail(email)
                .zipWith(userRoleDao.findRolesByEmail(email),
                          User::setRoles);
    }
    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(
                BCRYPT_STRENGTH);
    }
    public static boolean passwordIsHacked(String password) {
        return breachedPasswords
                .contains(password);
    }
    private static final Set<String> breachedPasswords = Set.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember");
    public static final int BCRYPT_STRENGTH = 7;
    public static final int MIN_PASSWORD_LENGTH = 12;
    public static final String EMAIL_REGEX = "(?i)\\w+(\\.\\w+){0,2}@acme.com";
    public static final String USER_EXISTS_ERRORMSG = "User exist!";
    public static final String PASSWORD_TOO_SHORT_ERRORMSG =
            "The password length must be at least " +
                    MIN_PASSWORD_LENGTH +
                    " chars!";
    public static final String PASSWORD_HACKED_ERRORMSG =
            "The password is in the hacker's database!";
    public static final String SAME_PASSWORD_ERRORMSG =
            "The passwords must be different!";
    public static final String PASSWORD_UPDATEMSG =
            "The password has been updated successfully";
}