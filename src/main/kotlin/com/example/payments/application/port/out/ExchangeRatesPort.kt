package com.example.payments.application.port.out

import com.example.payments.domain.ExchangeRate
import reactor.core.publisher.Flux

interface ExchangeRatesPort {
    fun get(): Flux<ExchangeRate>
}
