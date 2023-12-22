package com.example.payments.application.port.`in`

import com.example.payments.domain.ExchangeRate
import reactor.core.publisher.Flux

interface GetExchangeRatesOperation {
    fun execute(): Flux<ExchangeRate>
}
