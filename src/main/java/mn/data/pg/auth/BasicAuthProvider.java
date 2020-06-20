package mn.data.pg.auth;

import java.util.Optional;
import javax.inject.Singleton;

import org.reactivestreams.Publisher;
import io.reactivex.Flowable;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;

import mn.data.pg.services.UserService;
import mn.data.pg.dtos.UserDto;


import static io.micronaut.security.authentication.AuthenticationFailureReason.PASSWORD_EXPIRED;
import static io.micronaut.security.authentication.AuthenticationFailureReason.USER_NOT_FOUND;
import static java.util.Collections.singletonList;

@Singleton
public class BasicAuthProvider implements AuthenticationProvider {

    private final UserService userService;

    public BasicAuthProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest httpReq, AuthenticationRequest authReq) {

        final String username = authReq.getIdentity().toString();
        final String password = authReq.getSecret().toString();

        Optional<UserDto> existingUser = userService.findUser(username);

        return Flowable.just(
                existingUser.map( user -> {
                    if (user.getPassword().equals(password)) {
                        return new UserDetails(username, singletonList(user.getRole()));
                    }
                    return new AuthenticationFailed(PASSWORD_EXPIRED);
                })
                        .orElse(new AuthenticationFailed(USER_NOT_FOUND))
        );
    }
}