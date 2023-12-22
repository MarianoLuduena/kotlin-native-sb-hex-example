package com.example.payments.config

import com.example.payments.config.exception.BusinessException
import com.example.payments.config.exception.NotFoundException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.core.MethodParameter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebInputException
import java.lang.RuntimeException
import java.time.ZonedDateTime
import java.util.stream.Stream

class ErrorHandlerTest {

    private val httpRequest: ServerHttpRequest = Mockito.mock(ServerHttpRequest::class.java)
    private var handler: ErrorHandler? = null

    @BeforeEach
    fun setup() {
        handler = ErrorHandler()
        Mockito.`when`(httpRequest.method).thenReturn(HttpMethod.POST)
    }

    @Test
    fun willReturnAGenericError_WhenItGetsIntoTheDefaultMethod() {
        val actual = handler!!.handleDefault(RuntimeException(), httpRequest)

        val errorCode = ErrorCode.INTERNAL_ERROR
        val expected = buildProblemDetail(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode = errorCode,
            message = errorCode.reasonPhrase,
            timestamp = actual.body?.getTimestamp()!!
        )

        Assertions.assertThat(actual.body).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("serverWebInputExceptionSource")
    fun willReturnABadRequest_WhenServerWebInputExceptionIsCaught(
        methodParameter: MethodParameter?,
        parameterName: String?,
        errorMessage: String,
        expectedMessage: String
    ) {
        val ex =
            methodParameter?.let {
                val parameterType = String::class.java
                Mockito.`when`(it.parameterName).thenReturn(parameterName)
                Mockito.`when`(it.parameterType).thenReturn(parameterType)
                ServerWebInputException(errorMessage, methodParameter)
            } ?: ServerWebInputException(errorMessage)

        val actual = handler!!.handleBadRequest(ex, httpRequest)

        val errorCode = ErrorCode.BAD_REQUEST
        val expected = buildProblemDetail(
            httpStatus = HttpStatus.BAD_REQUEST,
            errorCode = errorCode,
            message = expectedMessage,
            timestamp = actual.body?.getTimestamp()!!
        )

        Assertions.assertThat(actual.statusCode.value()).isEqualTo(expected.status)
        Assertions.assertThat(actual.body).isEqualTo(expected)
    }

    @Test
    fun willReturnBadRequest_WhenConstraintViolationExceptionIsCaught() {
        val constraint = Mockito.mock(ConstraintViolation::class.java)
        val ex = ConstraintViolationException(ERROR_MESSAGE, setOf(constraint))

        val actual = handler!!.handleBadRequest(ex, httpRequest)

        val errorCode = ErrorCode.BAD_REQUEST
        val expected = buildProblemDetail(
            httpStatus = HttpStatus.BAD_REQUEST,
            errorCode = errorCode,
            message = ERROR_MESSAGE,
            timestamp = actual.body?.getTimestamp()!!
        )

        Assertions.assertThat(actual.statusCode.value()).isEqualTo(expected.status)
        Assertions.assertThat(actual.body).isEqualTo(expected)
    }

    @Test
    fun willReturnBadRequest_WhenConstraintViolationExceptionWithoutMessageIsCaught() {
        val ex = ConstraintViolationException(setOf())

        val actual = handler!!.handleBadRequest(ex, httpRequest)

        val errorCode = ErrorCode.BAD_REQUEST
        val expected = buildProblemDetail(
            httpStatus = HttpStatus.BAD_REQUEST,
            errorCode = errorCode,
            message = "",
            timestamp = actual.body?.getTimestamp()!!
        )

        Assertions.assertThat(actual.statusCode.value()).isEqualTo(expected.status)
        Assertions.assertThat(actual.body).isEqualTo(expected)
    }

    @Test
    fun willReturnNotFound_WhenNotFoundExceptionIsCaught() {
        val errorCode = ErrorCode.RESOURCE_NOT_FOUND
        val ex = NotFoundException(errorCode)

        val actual = handler!!.handleNotFound(ex, httpRequest)

        val expected = buildProblemDetail(
            httpStatus = HttpStatus.NOT_FOUND,
            errorCode = errorCode,
            message = errorCode.reasonPhrase,
            timestamp = actual.body?.getTimestamp()!!
        )

        Assertions.assertThat(actual.statusCode.value()).isEqualTo(expected.status)
        Assertions.assertThat(actual.body).isEqualTo(expected)
    }

    @Test
    fun willReturnUnprocessableEntity_WhenBusinessExceptionIsCaught() {
        val errorCode = ErrorCode.ACCOUNT_NOT_AVAILABLE
        val ex = BusinessException(errorCode)

        val actual = handler!!.handleUnprocessableEntity(ex, httpRequest)

        val expected = buildProblemDetail(
            httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
            errorCode = errorCode,
            message = errorCode.reasonPhrase,
            timestamp = actual.body?.getTimestamp()!!
        )

        Assertions.assertThat(actual.statusCode.value()).isEqualTo(expected.status)
        Assertions.assertThat(actual.body).isEqualTo(expected)
    }

    private fun buildProblemDetail(
        httpStatus: HttpStatus,
        message: String,
        errorCode: ErrorCode,
        timestamp: ZonedDateTime
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, message)
        problemDetail.setProperty("name", httpStatus.name)
        problemDetail.setProperty(TIMESTAMP_FIELD, timestamp)
        problemDetail.setProperty("code", errorCode.code)
        problemDetail.setProperty("method", HttpMethod.POST.name())
        return problemDetail
    }

    companion object {
        private const val TIMESTAMP_FIELD = "timestamp"
        private const val ERROR_MESSAGE = "someErrorMessage"

        private fun ProblemDetail?.getTimestamp(): ZonedDateTime? =
            this?.properties?.getValue(TIMESTAMP_FIELD) as ZonedDateTime?

        @JvmStatic
        private fun serverWebInputExceptionSource(): Stream<Arguments> =
            Stream.of(
                Arguments.of(null, null, ERROR_MESSAGE, ErrorCode.BAD_REQUEST.reasonPhrase),
                Arguments.of(
                    Mockito.mock(MethodParameter::class.java),
                    null,
                    ERROR_MESSAGE,
                    "Invalid input for field '' (expected type 'String')"
                ),
                Arguments.of(
                    Mockito.mock(MethodParameter::class.java),
                    "aParameterName",
                    ERROR_MESSAGE,
                    "Invalid input for field 'aParameterName' (expected type 'String')"
                )
            )
    }

}
