package account.model;

import account.model.records.request.RecordRequestSignup;
import account.model.records.response.RecordResponseSignup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("USER")
public class User implements UserDetails {

    private static final User UNKNOWN =
            User.builder().id(-1).build();

    @Id
    private long id;
    private String name;
    private String lastname;
    private String email;
    private String password;
    @Column("account_locked")
    @Builder.Default()
    private boolean accountLocked = false;

    @Column("failed_logins")
    @Builder.Default()
    private int failedLogins = 0;

    @Builder.Default()
    @Transient
    private List<String> roles = new ArrayList<>();

    public static User fromSignupRequest(RecordRequestSignup request,
                                         String encrPass) {
        return User
                   .builder()
                   .name(request.name())
                   .lastname(request.lastname())
                   .email(request.email())
                   .password(encrPass)
                   .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils
                .createAuthorityList(
                                 roles.toArray(String[]::new));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public RecordResponseSignup toSignupResponse() {
        return new RecordResponseSignup(id,
                                        name,
                                        lastname,
                                        email,
                                        roles);
    }

    public static User unknown() {
        return UNKNOWN;
    }

    public boolean isUnknown() {
        return id == -1;
    }
}