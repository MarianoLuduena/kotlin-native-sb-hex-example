package com.example.payments.application.usecase

import com.example.payments.application.port.`in`.GetExchangeRatesOperation
import com.example.payments.application.port.out.ExchangeRatesPort
import com.example.payments.domain.ExchangeRate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class GetExchangeRatesUseCase(
    private val exchangeRatesPort: ExchangeRatesPort
): GetExchangeRatesOperation {

    override fun execute(): Flux<ExchangeRate> {
        LOG.info("Getting exchange rates")
        return exchangeRatesPort.get()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GetExchangeRatesUseCase::class.java)
    }

}
