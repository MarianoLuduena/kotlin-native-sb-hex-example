package com.example.payments.application.usecase

import com.example.payments.application.port.out.ExchangeRatesPort
import com.example.payments.mock.ExchangeRateMockFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class GetExchangeRatesUseCaseTest {

    private val exchangeRatesPort = Mockito.mock(ExchangeRatesPort::class.java)

    private var useCase: GetExchangeRatesUseCase? = null

    @BeforeEach
    fun setup() {
        useCase = GetExchangeRatesUseCase(exchangeRatesPort)
    }

    @Test
    fun willReturnAListOfRates_WhenExecuteIsCalled() {
        val firstElement = ExchangeRateMockFactory.exchangeRate()
        val secondElement = firstElement.copy(name = "A different copy")

        Mockito.`when`(exchangeRatesPort.get()).thenReturn(Flux.just(firstElement, secondElement))

        val actual = useCase!!.execute()

        StepVerifier.create(actual)
            .expectNext(firstElement)
            .expectNext(secondElement)
            .verifyComplete()

        Mockito.verify(exchangeRatesPort, Mockito.times(1)).get()
    }

}
