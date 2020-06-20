package mn.data.pg.auth

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
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

class LoginAuthenticationSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    def "Verify unauthenticated access does not work"() {

        when: 'Accessing a secured URL without authenticating'
        client.toBlocking().exchange(HttpRequest.GET('/messages',)) // <4>

        then: 'returns unauthorized'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    def "Try to login with a non existing user and fail"() {

        given: 'User data'
        String username = 'non-existing'
        String password = 'random-pass'

        when: 'Login endpoint is called with an unknown username'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials)
        HttpResponse<BearerAccessRefreshToken> response = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'returns unauthorized'
        !response
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
        e.message == 'User Not Found'
    }

    def "Try to login with an existing user but wrong password and fail"() {

        given: 'User data'
        String username = 'user1'
        String password = 'random-pass'

        when: 'Login endpoint is called with an unknown username'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials) // <5>
        HttpResponse<BearerAccessRefreshToken> response = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'returns unauthorized'
        !response
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
        e.message == 'Password Expired'
    }

    def "Login with an existing user correctly"() {

        given: 'User data'
        String username = 'user1'
        String password = 'password1'

        when: 'Login endpoint is called with valid credentials'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'JWT token is returned'
        rsp.status == HttpStatus.OK
        rsp.body().username == username
        rsp.body().accessToken
        JWTParser.parse(rsp.body().accessToken) instanceof SignedJWT
        JWTParser.parse(rsp.body().accessToken).getJWTClaimsSet().getSubject() == username
        rsp.body().refreshToken
        JWTParser.parse(rsp.body().refreshToken) instanceof SignedJWT
    }

    def "Login with a forbidden role obtaining access denied"() {

        given: 'User data'
        String username = 'user3'
        String password = 'password3'

        when: 'Login endpoint is called with valid credentials'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'JWT token is returned'
        rsp.status == HttpStatus.OK
        rsp.body().username == username
        rsp.body().accessToken
        JWTParser.parse(rsp.body().accessToken) instanceof SignedJWT
        JWTParser.parse(rsp.body().refreshToken) instanceof SignedJWT

        when:
        String accessToken = rsp.body().accessToken
        HttpRequest requestWithAuthorization = HttpRequest.GET('/messages/all').header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        HttpResponse<String> response = client.toBlocking().exchange(requestWithAuthorization, String)

        then:
        !response
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.FORBIDDEN
    }

}