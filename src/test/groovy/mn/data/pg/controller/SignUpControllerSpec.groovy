package mn.data.pg.controller

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import mn.data.pg.dtos.UserDto

class SignUpControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    def "Sign-up providing wrong payload with illegal id"() {

        given: 'Incorrect user data'
        String username = 'user1'
        String password = 'wrong-pwd-indeed'

        when: 'Sign-up endpoint is called with invalid credentials'
        HttpRequest request = HttpRequest.POST('/signup', UserDto.builder().username(username).password(password).build())
        HttpResponse<BearerAccessRefreshToken> response = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'This user already exists'
        !response
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
    }

    def "Sign-up with a non existing user correctly"() {

        given: 'User data'
        String username = 'user4'
        String password = 'password4'

        when: 'Sign-up endpoint is called with a payload'
        HttpRequest request = HttpRequest.POST('/signup', UserDto.builder().username(username).password(password).build())
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'The user has been created'
        rsp.status == HttpStatus.OK
        rsp.body().username == username

    }

    def "Login with the new user correctly"() {

        given: 'User data'
        String username = 'user4'
        String password = 'password4'

        when: 'Login endpoint is called with valid credentials'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'JWT token is returned'
        rsp.status == HttpStatus.OK
        rsp.body().username == username
        rsp.body().accessToken
        JWTParser.parse(rsp.body().accessToken) instanceof SignedJWT
        rsp.body().refreshToken
    }

}