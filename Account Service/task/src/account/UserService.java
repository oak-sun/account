package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserDao dao;

    public User save(User user) {
        return dao.save(user);
    }
    public Optional<User> findUserByEmail(String email) {
        return dao.findUserByEmail(email);
    }
}
