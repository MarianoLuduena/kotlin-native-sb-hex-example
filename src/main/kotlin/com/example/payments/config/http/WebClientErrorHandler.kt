package com.example.payments.config.http

import com.example.payments.adapter.rest.exception.BadRequestWebClientException
import com.example.payments.adapter.rest.exception.WebClientGenericException
import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.BusinessException
import com.example.payments.config.exception.GenericException
import com.example.payments.config.exception.NotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import kotlin.reflect.KFunction1

class WebClientErrorHandler(private val objectMapper: ObjectMapper) {

    fun handle(response: ClientResponse): Mono<ClientResponse> =
        if (hasError(response)) {
            handleError(response)
        } else {
            Mono.just(response)
        }

    private fun hasError(response: ClientResponse): Boolean = response.statusCode().isError

    private fun handleError(response: ClientResponse): Mono<ClientResponse> =
        DataBufferUtils.join(response.body(BodyExtractors.toDataBuffers()))
            .flatMap { dataBuffer ->
                val httpStatus = response.statusCode()
                val body = parseResponseBody(dataBuffer)
                val code = CODES_BY_STATUS_CODE.getOrDefault(httpStatus, ErrorCode.INTERNAL_ERROR.code)
                val description = body.error
                val errorCode = ErrorCode(code, description)
                val exception =
                    EXCEPTIONS_BY_STATUS_CODE.getOrElse(httpStatus) { ::WebClientGenericException }(errorCode)
                Mono.error(exception)
            }

    private fun parseResponseBody(dataBuffer: DataBuffer): ErrorBody =
        dataBuffer.asInputStream()
            .use { inputStream -> objectMapper.readValue(inputStream, ErrorBody::class.java) }

    companion object {
        private val EXCEPTIONS_BY_STATUS_CODE: Map<HttpStatusCode, KFunction1<ErrorCode, GenericException>> = mapOf(
            Pair(HttpStatus.BAD_REQUEST, ::BadRequestWebClientException),
            Pair(HttpStatus.NOT_FOUND, ::NotFoundException),
            Pair(HttpStatus.UNPROCESSABLE_ENTITY, ::BusinessException),
        )

        private val CODES_BY_STATUS_CODE = mapOf(
            Pair(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST.code),
            Pair(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.code)
        )
    }

}
