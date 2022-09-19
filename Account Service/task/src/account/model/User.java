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
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entity class connected to R2DBC-Table LOGIN, that implements the UserDetails interface of Spring Security.
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("USER")
public class User implements UserDetails {

    private static final User UNKNOWN = User
            .builder()
            .id(-1)
            .build();

    @Id
    private long id;
    private String name;
    private String lastname;
    private String email;
    private String password;

    @Builder.Default()
    @Transient
    private List<String> roles = new ArrayList<>();

    public static User fromSignupRequest(RecordRequestSignup request,
                                         String encryptedPassword) {
        return User
                .builder()
                .name(request.name())
                .lastname(request.lastname())
                .email(request.email())
                .password(encryptedPassword)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils
                .createAuthorityList(roles.toArray(
                        String[]::new));
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
        return true;
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