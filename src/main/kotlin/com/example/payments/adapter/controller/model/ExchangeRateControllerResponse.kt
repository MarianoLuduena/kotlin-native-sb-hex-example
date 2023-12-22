package com.example.payments.adapter.controller.model

import com.example.payments.domain.ExchangeRate
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ExchangeRateControllerResponse(
    val buy: BigDecimal?,
    val sell: BigDecimal,
    val source: String,
    val name: String,
    val currency: String,
    val updatedAt: ZonedDateTime
) {

    companion object {
        fun from(domain: ExchangeRate) =
            ExchangeRateControllerResponse(
                buy = domain.buy,
                sell = domain.sell,
                source = domain.source,
                name = domain.name,
                currency = domain.currency.currencyCode,
                updatedAt = domain.updatedAt
            )
    }

}
