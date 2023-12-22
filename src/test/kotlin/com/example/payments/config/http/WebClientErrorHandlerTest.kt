package com.example.payments.config.http

import com.example.payments.adapter.rest.exception.BadRequestWebClientException
import com.example.payments.adapter.rest.exception.WebClientGenericException
import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.BusinessException
import com.example.payments.config.exception.GenericException
import com.example.payments.config.exception.NotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Flux
import reactor.kotlin.test.expectError
import reactor.test.StepVerifier
import java.util.stream.Stream

class WebClientErrorHandlerTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val dataBufferFactory = DefaultDataBufferFactory()

    private var handler: WebClientErrorHandler? = null

    @BeforeEach
    fun setup() {
        handler = WebClientErrorHandler(objectMapper)
    }

    @Test
    fun willReturnTheResponse_WhenThereIsNoError() {
        val clientResponse = Mockito.mock(ClientResponse::class.java)
        Mockito.`when`(clientResponse.statusCode()).thenReturn(HttpStatus.OK)

        val actual = handler!!.handle(clientResponse)

        StepVerifier.create(actual)
            .expectNext(clientResponse)
            .verifyComplete()
    }

    @ParameterizedTest
    @MethodSource("exceptionsByStatusCode")
    fun willReturnAnException_WhenTheResponseIsError(
        httpStatus: HttpStatus,
        errorBody: String,
        expected: GenericException
    ) {
        val clientResponse = Mockito.mock(ClientResponse::class.java)

        Mockito.`when`(clientResponse.statusCode()).thenReturn(httpStatus)

        Mockito.`when`(clientResponse.body<Flux<DataBuffer>>(any()))
            .thenReturn(
                DataBufferUtils.readInputStream(
                    { errorBody.byteInputStream() },
                    dataBufferFactory,
                    BUFFER_SIZE
                )
            )

        val actual = handler!!.handle(clientResponse)

        StepVerifier.create(actual)
            .expectErrorMatches { it::class.java == expected::class.java && it.message == expected.message }
            .verify()
    }

    @Test
    fun willReturnAnUnmanagedException_WhenParsingTheResponseFails() {
        val clientResponse = Mockito.mock(ClientResponse::class.java)

        Mockito.`when`(clientResponse.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR)

        Mockito.`when`(clientResponse.body<Flux<DataBuffer>>(any()))
            .thenReturn(
                DataBufferUtils.readInputStream(
                    { "{}".byteInputStream() },
                    dataBufferFactory,
                    BUFFER_SIZE
                )
            )

        val actual = handler!!.handle(clientResponse)

        StepVerifier.create(actual)
            .expectError(MismatchedInputException::class)
            .verify()
    }

    companion object {
        private const val ERROR_MESSAGE = "an error message"
        private const val ERROR_BODY = "{ \"error\": \"${ERROR_MESSAGE}\" }"
        private const val BUFFER_SIZE = 256

        @JvmStatic
        private fun exceptionsByStatusCode(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    HttpStatus.BAD_REQUEST,
                    ERROR_BODY,
                    BadRequestWebClientException(ErrorCode(ErrorCode.BAD_REQUEST.code, ERROR_MESSAGE))
                ),
                Arguments.of(
                    HttpStatus.NOT_FOUND,
                    ERROR_BODY,
                    NotFoundException(ErrorCode(ErrorCode.RESOURCE_NOT_FOUND.code, ERROR_MESSAGE))
                ),
                Arguments.of(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    ERROR_BODY,
                    BusinessException(ErrorCode(ErrorCode.INTERNAL_ERROR.code, ERROR_MESSAGE))
                ),
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ERROR_BODY,
                    WebClientGenericException(ErrorCode(ErrorCode.INTERNAL_ERROR.code, ERROR_MESSAGE))
                ),
            )
    }

}
