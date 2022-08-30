package account.controllers;

import account.UserService;
import account.Password;
import account.User;
import account.security.exception.HackerPasswordException;
import account.security.exception.MinimumPasswordException;
import account.security.exception.SamePasswordException;
import account.security.exception.UserExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/auth")
public class ApiController {

    @Autowired
    UserService service;

    @Autowired
    PasswordEncoder encoder;

    List<String> hackerPass = List.of(
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

    @PostMapping("signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody User user) {

        if (hackerPass.contains(user.getPassword())) {
            throw new HackerPasswordException();
        }

        if (user.getPassword().length() < 12) {
            throw new MinimumPasswordException();
        }
        user.setPassword(encoder.encode(user.getPassword()));

        if (service.findUserByEmail(user
                                       .getEmail()
                                       .toLowerCase())
                                       .isPresent()) {
            throw new UserExistException();
        }

        user.setEmail(user.getEmail().toLowerCase());
        return new ResponseEntity<>(service.save(user),
                                    HttpStatus.OK);
    }

    @PostMapping("changepass")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody Password newPassMap,
                                                 @AuthenticationPrincipal
                                                 UserDetails details) {
        if (details != null) {
            Optional<User> userOptional = service.findUserByEmail(details.getUsername());

            if (userOptional.isPresent()) {

                var user = userOptional.get();
                var newPass = newPassMap.getNew_password();

                if (hackerPass.contains(newPass)) {
                    throw new HackerPasswordException();
                }
                if (encoder.matches(newPass,
                                    user.getPassword())){
                    throw new SamePasswordException();
                }
                if (newPass.length() < 12) {
                    throw new MinimumPasswordException();
                }

                user.setPassword(encoder.encode(newPass));
                service.save(user);
                Map<String, String> response = new HashMap<>();
                response.put("email", user.getEmail());
                response.put("status",
                                 "The password has been updated successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}