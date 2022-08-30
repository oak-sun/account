package account.controllers;

import account.User;
import account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class Controller {
    @Autowired
    UserService service;

    @GetMapping("api/empl/payment")
    public ResponseEntity<User> getEmployPayroll(@AuthenticationPrincipal
                                                     UserDetails details) {

        if (details != null) {
            Optional<User> user = service
                    .findUserByEmail(details.getUsername());

            if (user.isPresent()) {
                return new ResponseEntity<>(user.get(),
                                            HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

}