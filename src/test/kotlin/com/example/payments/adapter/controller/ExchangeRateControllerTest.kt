package com.example.payments.adapter.controller

import com.example.payments.application.port.`in`.GetExchangeRatesOperation
import com.example.payments.mock.ExchangeRateMockFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux

@WebFluxTest(ExchangeRateController::class)
class ExchangeRateControllerTest(
    @Autowired private val webClient: WebTestClient,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @MockBean
    private val getExchangeRatesOperation: GetExchangeRatesOperation? = null

    @Test
    fun willReturnAListOfRates_WhenGetEndpointIsCalled() {
        Mockito.`when`(getExchangeRatesOperation!!.execute()).thenReturn(Flux.just(ExchangeRateMockFactory.exchangeRate()))

        val expected =
            objectMapper.writeValueAsString(
                listOf(
                    ExchangeRateMockFactory.exchangeRateControllerResponse()
                )
            )

        webClient.get()
            .uri(BASE_URL)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json(expected, true)
            .returnResult()

        Mockito.verify(getExchangeRatesOperation, Mockito.times(1)).execute()
    }

    companion object {
        private const val BASE_URL = "/exchange-rates"
    }

}
