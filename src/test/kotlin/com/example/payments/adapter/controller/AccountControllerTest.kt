package com.example.payments.adapter.controller

import com.example.payments.application.port.`in`.TransferMoneyOperation
import com.example.payments.config.ErrorCode
import com.example.payments.mock.AccountMockFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(AccountController::class)
class AccountControllerTest(
    @Autowired private val webClient: WebTestClient,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @MockBean
    private val transferMoneyOperation: TransferMoneyOperation? = null

    @Test
    fun willReturnOk_WhenTheTransferHasCompleted() {
        val expectedCmd = AccountMockFactory.transferMoneyOperationCommand()
        Mockito.`when`(transferMoneyOperation!!.execute(expectedCmd)).thenReturn(Mono.just(Unit))

        val request = objectMapper.writeValueAsString(AccountMockFactory.transferMoneyControllerRequest())

        val expected = objectMapper.writeValueAsString(AccountMockFactory.transferMoneyControllerResponse())

        webClient.post()
            .uri("$BASE_URL/{id}/transfers", expectedCmd.sourceAccount)
            .bodyValue(request)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isOk
            .expectBody().json(expected, true)
            .returnResult()

        Mockito.verify(transferMoneyOperation, Mockito.times(1)).execute(expectedCmd)
    }

    @Test
    fun willReturn400_WhenTheRequestedIdIsMalformed() {
        val request = objectMapper.writeValueAsString(AccountMockFactory.transferMoneyControllerRequest())

        val expected = "{" +
                "\"title\": \"Bad Request\"," +
                "\"status\": 400," +
                "\"detail\": \"Invalid input for field 'id' (expected type 'long')\"," +
                "\"code\": \"${ErrorCode.BAD_REQUEST.code}\"" +
                "}"

        webClient.post()
            .uri("$BASE_URL/{id}/transfers", "ABC")
            .bodyValue(request)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody().json(expected, false)
            .returnResult()
    }

    companion object {
        private const val BASE_URL = "/accounts"
    }

}
