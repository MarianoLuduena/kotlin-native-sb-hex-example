package com.example.payments.config

import com.example.payments.config.exception.BusinessException
import com.example.payments.config.exception.NotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebInputException
import java.time.ZoneOffset
import java.time.ZonedDateTime

@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(Throwable::class)
    fun handleDefault(ex: Throwable, serverHttpRequest: ServerHttpRequest): ResponseEntity<ProblemDetail> {
        val httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        LOG.error(httpStatus.reasonPhrase, ex)
        val errorCode = ErrorCode.INTERNAL_ERROR
        return buildProblemDetail(httpStatus, errorCode.reasonPhrase, errorCode, serverHttpRequest)
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleBadRequest(
        ex: ServerWebInputException,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<ProblemDetail> {
        val httpStatus = HttpStatus.BAD_REQUEST
        LOG.error(httpStatus.reasonPhrase, ex)
        val errorCode = ErrorCode.BAD_REQUEST
        val message =
            ex.methodParameter?.let {
                val parameterName = it.parameterName.orEmpty()
                val parameterType = it.parameterType.simpleName
                "Invalid input for field '${parameterName}' (expected type '${parameterType}')"
            } ?: errorCode.reasonPhrase
        return buildProblemDetail(httpStatus, message, errorCode, serverHttpRequest)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleBadRequest(
        ex: ConstraintViolationException,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<ProblemDetail> {
        val httpStatus = HttpStatus.BAD_REQUEST
        LOG.error(httpStatus.reasonPhrase, ex)
        val message = ex.message.orEmpty()
        return buildProblemDetail(httpStatus, message, ErrorCode.BAD_REQUEST, serverHttpRequest)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(
        ex: NotFoundException,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<ProblemDetail> {
        val httpStatus = HttpStatus.NOT_FOUND
        LOG.error(httpStatus.reasonPhrase, ex)
        return buildProblemDetail(httpStatus, ex, ex.errorCode, serverHttpRequest)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleUnprocessableEntity(
        ex: BusinessException,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<ProblemDetail> {
        val httpStatus = HttpStatus.UNPROCESSABLE_ENTITY
        LOG.error(httpStatus.reasonPhrase, ex)
        return buildProblemDetail(httpStatus, ex, ex.errorCode, serverHttpRequest)
    }

    private fun buildProblemDetail(
        httpStatus: HttpStatus,
        ex: Throwable,
        errorCode: ErrorCode,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<ProblemDetail> =
        buildProblemDetail(httpStatus, ex.message.orEmpty(), errorCode, serverHttpRequest)

    private fun buildProblemDetail(
        httpStatus: HttpStatus,
        message: String,
        errorCode: ErrorCode,
        serverHttpRequest: ServerHttpRequest
    ): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, message)
        problemDetail.setProperty("name", httpStatus.name)
        problemDetail.setProperty("timestamp", ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC))
        problemDetail.setProperty("code", errorCode.code)
        problemDetail.setProperty("method", serverHttpRequest.method.name())
        return ResponseEntity(problemDetail, httpStatus)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ErrorHandler::class.java)
    }

}
