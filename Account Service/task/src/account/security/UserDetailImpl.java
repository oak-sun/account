package account.security;

import account.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailImpl implements UserDetails {
    private final String username;
    private final String password;
    private final List<GrantedAuthority> grantedList;

    public UserDetailImpl(User user) {
        this.username = user
                            .getEmail()
                            .toLowerCase();
        this.password = user.getPassword();
        this.grantedList = List.of(
                new SimpleGrantedAuthority("USER"));
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.grantedList;
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
}
