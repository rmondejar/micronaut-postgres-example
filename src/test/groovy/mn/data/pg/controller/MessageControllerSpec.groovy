package mn.data.pg.controller

import java.time.Instant

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import mn.data.pg.dtos.MessageDto

class MessageControllerSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    @Shared
    String validToken

    @Shared
    Integer numMessages = 1


    def "Login with an existing user correctly"() {

        given: 'User data'
        String username = 'user2'
        String password = 'password2'

        when: 'Login endpoint is called with valid credentials'
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password)
        HttpRequest request = HttpRequest.POST('/login', credentials)
        HttpResponse<BearerAccessRefreshToken> response = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        validToken = response?.body()?.accessToken

        then: 'JWT token is returned'
        response.status == HttpStatus.OK
        response.body()
        response.body().username == username
        validToken
        JWTParser.parse(validToken) instanceof SignedJWT
    }

    def "Retrieve messages from one validate user correctly"() {

        given: 'User data'
        String username = 'user2'

        when: 'Message endpoint is called with the token'
        HttpRequest requestWithAuthorization = HttpRequest.GET('/messages').header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        HttpResponse<List<MessageDto>> response = client.toBlocking().exchange(requestWithAuthorization, List)
        List<MessageDto> messages = response.body()
        numMessages = messages?.size()

        then:
        noExceptionThrown()
        response.status == HttpStatus.OK
        messages
        numMessages == 1

        messages.first()
        messages.first().username == username
    }

    def "Create new message from one validate user correctly"() {

        given: 'User data'
        String username = 'user2'
        String content = 'test message content'

        when: 'The message post endpoint is called with a valid token'
        HttpRequest requestWithAuthorization = HttpRequest.POST('/messages', content).header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        HttpResponse<MessageDto> response = client.toBlocking().exchange(requestWithAuthorization, MessageDto)

        then: 'The user\'s message is posted correctly'
        noExceptionThrown()
        response.status == HttpStatus.CREATED
        response
        response.body()
        response.body().id
        response.body().content
        response.body().content == content
        response.body().creationDate
        response.body().creationDate < Instant.now()


        when: 'recover messages of the user again'
        requestWithAuthorization = HttpRequest.GET('/messages').header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        HttpResponse<List<MessageDto>> response2 = client.toBlocking().exchange(requestWithAuthorization, List)
        List<MessageDto> messages = response2.body()

        then: 'The user\'s messages is increased and the new message is there'
        noExceptionThrown()
        response2.status == HttpStatus.OK
        messages
        !messages.empty
        messages?.size() == numMessages+1
        messages.last()
        messages.last().content == content
        messages.last().username == username
    }
}
