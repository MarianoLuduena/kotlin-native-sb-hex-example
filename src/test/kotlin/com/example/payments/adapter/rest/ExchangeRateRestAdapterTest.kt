package com.example.payments.adapter.rest

import com.example.payments.config.TestConfig
import com.example.payments.config.http.HttpConfig
import com.example.payments.extension.mockJsonResponse
import com.example.payments.mock.ExchangeRateMockFactory
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@RestClientTest
@Import(HttpConfig::class)
class ExchangeRateRestAdapterTest(
    @Autowired private val webClient: WebClient,
    @Autowired private val objectMapper: ObjectMapper,
) {

    private val mockWebServer = MockWebServer()
    private var adapter: ExchangeRateRestAdapter? = null

    @BeforeEach
    fun setup() {
        mockWebServer.start()
        val updatedConfig = BASE_CONFIG.copy(
            exchangeRateOutPort = BASE_CONFIG.exchangeRateOutPort.copy(
                mockWebServer.url(BASE_PATH).toString()
            )
        )
        adapter = ExchangeRateRestAdapter(webClient, updatedConfig)
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun willReturnAListOfRates_WhenGetIsCalled() {
        val response = objectMapper.writeValueAsString(listOf(ExchangeRateMockFactory.exchangeRateRestResponse()))
        mockWebServer.mockJsonResponse(response)

        val actual = adapter!!.get()

        StepVerifier.create(actual)
            .expectNext(ExchangeRateMockFactory.exchangeRate())
            .verifyComplete()

        val request = mockWebServer.takeRequest()
        Assertions.assertThat(request.path).isEqualTo(BASE_PATH)
        Assertions.assertThat(request.method).isEqualTo(HttpMethod.GET.name())
        Assertions.assertThat(request.getHeader(HttpHeaders.ACCEPT)).contains(MediaType.APPLICATION_JSON_VALUE)
    }

    companion object {
        private val BASE_CONFIG = TestConfig.config()
        private const val BASE_PATH = "/exchange-rates"
    }

}
