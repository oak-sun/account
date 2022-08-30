package account.security;


import account.User;
import account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    UserService service;

    @Override
    public UserDetails loadUserByUsername(String username)
                       throws UsernameNotFoundException {

        Optional<User> user = service.findUserByEmail(
                              username.toLowerCase());
        if(user.isPresent()) {
            return new UserDetailImpl(user.get());

        } else {
            throw new UsernameNotFoundException(
                    "Not found: " + username);
        }
    }
}
