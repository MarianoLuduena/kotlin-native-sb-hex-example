package com.example.payments.adapter.rest.model

import com.example.payments.domain.ExchangeRate
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.Currency

data class ExchangeRateRestResponse(
    @JsonProperty("compra") val buy: BigDecimal?,
    @JsonProperty("venta") val sell: BigDecimal,
    @JsonProperty("casa") val source: String,
    @JsonProperty("nombre") val name: String,
    @JsonProperty("moneda") val currency: String,
    @JsonProperty("fechaActualizacion") val updatedAt: ZonedDateTime
) {

    fun toDomain(): ExchangeRate =
        ExchangeRate(
            buy = buy,
            sell = sell,
            source = source,
            name = name,
            currency = Currency.getInstance(currency),
            updatedAt = updatedAt
        )

}
