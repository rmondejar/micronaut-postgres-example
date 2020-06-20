package mn.data.pg.auth

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
import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class OauthAccessTokenSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    def "Verify tokens are working"() {

        given: 'User data'
        String username = 'user1'
        String password = 'password1'

        when: 'Login endpoint is called with valid credentials'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        String authToken = rsp?.body()?.accessToken
        String refreshToken = rsp?.body()?.refreshToken

        then: 'JWT token is returned'
        rsp
        rsp.status == HttpStatus.OK
        rsp.body()
        rsp.body().username == username
        authToken
        JWTParser.parse(authToken) instanceof SignedJWT
        JWTParser.parse(authToken).getJWTClaimsSet().getSubject() == username
        refreshToken
        JWTParser.parse(refreshToken) instanceof SignedJWT

        when:
        HttpResponse<AccessRefreshToken> response = client.toBlocking().exchange(HttpRequest.POST('/oauth/access_token',
                new TokenRefreshRequest("refresh_token", refreshToken)), AccessRefreshToken)

        then:
        response.status == HttpStatus.OK
        response.body().accessToken
        response.body().refreshToken == refreshToken
    }

    def "Trying to refresh with wrong tokens returning errors"() {

        when: "using a malformed token"
        HttpResponse<AccessRefreshToken> response = client.toBlocking().exchange(HttpRequest.POST('/oauth/access_token',
                new TokenRefreshRequest("refresh_token", "notJwtTokenLikeAsItIsExpected")), AccessRefreshToken)

        then: "bad request response"
        !response
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST

        when: "using a non existing token"
        response = client.toBlocking().exchange(HttpRequest.POST('/oauth/access_token',
                new TokenRefreshRequest("refresh_token", "eyJhbGciOiJIUzI1NiJ9.MzZjZWQwNzktNmIxNi00OTNlLTg1ZjEtM2RjZTA4NGJiNWY2._grvHNUjh71cJf_e2VWnAGEUJEyJ61aT-1_vcWpk9lc")), AccessRefreshToken)

        then: "forbidden response"
        !response
        e = thrown(HttpClientResponseException)
        e.status == HttpStatus.FORBIDDEN
    }
}