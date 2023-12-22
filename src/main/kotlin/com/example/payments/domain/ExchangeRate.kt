package com.example.payments.domain

import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.Currency

data class ExchangeRate(
    val buy: BigDecimal?,
    val sell: BigDecimal,
    val source: String,
    val name: String,
    val currency: Currency,
    val updatedAt: ZonedDateTime
)
