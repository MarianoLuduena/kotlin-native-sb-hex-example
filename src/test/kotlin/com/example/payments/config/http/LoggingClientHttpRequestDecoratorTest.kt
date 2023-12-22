package com.example.payments.config.http

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.slf4j.LoggerFactory
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.ClientHttpRequest
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.net.URI

@ExtendWith(OutputCaptureExtension::class)
class LoggingClientHttpRequestDecoratorTest {

    private val clientHttpRequest = Mockito.mock(ClientHttpRequest::class.java)
    private val logger = LoggerFactory.getLogger("decorator-test")
    private val dataBufferFactory = DefaultDataBufferFactory()

    private var decorator: LoggingClientHttpRequestDecorator? = null

    @BeforeEach
    fun setup() {
        decorator = LoggingClientHttpRequestDecorator(clientHttpRequest, logger)
    }

    @Test
    fun willLogTheRequest_WhenWriteWithIsCalled(output: CapturedOutput) {
        Mockito.`when`(clientHttpRequest.writeWith(any())).thenAnswer { invocation ->
            invocation.getArgument<Flux<DataBuffer>>(0).then()
        }

        val headers = HttpHeaders()
        headers.contentLength = CONTENT_LENGTH
        Mockito.`when`(clientHttpRequest.headers).thenReturn(headers)
        Mockito.`when`(clientHttpRequest.method).thenReturn(HttpMethod.POST)
        Mockito.`when`(clientHttpRequest.uri).thenReturn(REQUEST_URI)

        val body = DataBufferUtils.readInputStream({ CONTENT.byteInputStream() }, dataBufferFactory, BUFFER_SIZE)

        val actual = decorator!!.writeWith(body)

        StepVerifier.create(actual)
            .verifyComplete()

        Assertions.assertThat(output.out).contains("POST $REQUEST_URI | {Content-Length=[${CONTENT_LENGTH}]} $CONTENT")
    }

    @Test
    fun willLogTheLengthOfTheRequest_WhenWriteWithIsCalledWithAHighContentLength(output: CapturedOutput) {
        Mockito.`when`(clientHttpRequest.writeWith(any())).thenAnswer { invocation ->
            invocation.getArgument<Flux<DataBuffer>>(0).then()
        }

        val headers = HttpHeaders()
        headers.contentLength = CONTENT_LENGTH + 1
        Mockito.`when`(clientHttpRequest.headers).thenReturn(headers)
        Mockito.`when`(clientHttpRequest.method).thenReturn(HttpMethod.POST)
        Mockito.`when`(clientHttpRequest.uri).thenReturn(REQUEST_URI)

        val body = DataBufferUtils.readInputStream({ CONTENT.byteInputStream() }, dataBufferFactory, BUFFER_SIZE)

        val actual = decorator!!.writeWith(body)

        StepVerifier.create(actual)
            .verifyComplete()

        Assertions.assertThat(output.out)
            .contains("POST $REQUEST_URI | {Content-Length=[${headers.contentLength}]} " +
                    "[${headers.contentLength} bytes]")
    }

    companion object {
        private const val CONTENT = "{ \"foo\": \"bar\" }"
        private const val CONTENT_LENGTH = 4L * 1024L
        private const val BUFFER_SIZE = 256
        private val REQUEST_URI = URI.create("http://localhost:1234")
    }

}
