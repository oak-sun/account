package account.security;

import account.service.AccountService;
import account.service.AdminService;
import account.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfigurer {

    @Bean
    @Autowired
    public RouterFunction<ServerResponse> routes(AuthService auth,
                                                 AccountService account,
                                                 AdminService admin) {
        return route()
                .add(authenticationRoutes(auth))
                .add(accountRoutes(account))
                .add(adminRoutes(admin))
                .build();
    }

    private RouterFunction<ServerResponse> authenticationRoutes(
                                                 AuthService auth) {
        return route()
                .POST("/api/auth/signup",
                        auth::signup)
                .POST("/api/auth/changepass",
                        auth::changePassword)
                .build();
    }

    private RouterFunction<ServerResponse> adminRoutes(
                                               AdminService admin) {
        return route()
                .GET("/api/admin/user",
                        admin::displayUsers)
                .DELETE("/api/admin/user/{email}",
                        admin::deleteUser)
                .PUT("/api/admin/user/role",
                        admin::toggleRole)
                .build();
    }

    private RouterFunction<ServerResponse> accountRoutes(
                                            AccountService account) {
        return route()
                .GET("/api/empl/payment",
                        account::accessPayrolls)
                .POST("/api/acct/payments",
                        account::uploadPayrolls)
                .PUT("/api/acct/payments",
                        account::changePayrolls)
                .build();
    }
}
