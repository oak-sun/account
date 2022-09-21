import account.security.exception.ErrorMessenger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class ErrorMessengerTest {

    @Test
    void buildShouldReturnMessageWithFiveFields(){

        var exceptedMessage = new ErrorMessenger(LocalDateTime.MIN,
                 400,
                "someError",
                "someMessage",
                "/some/path");

        var actualMessage =  ErrorMessenger.builder()
                .timestamp(LocalDateTime.MIN)
                .status(400)
                .error("someError")
                .message("someMessage")
                .path("/some/path")
                .build();
        Assertions.assertEquals(exceptedMessage, actualMessage);
    }
}
