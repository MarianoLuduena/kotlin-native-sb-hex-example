package com.example.payments.mock

import com.example.payments.adapter.controller.model.ExchangeRateControllerResponse
import com.example.payments.adapter.rest.model.ExchangeRateRestResponse
import com.example.payments.domain.ExchangeRate
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.Currency

object ExchangeRateMockFactory {

    private val BUY = BigDecimal.valueOf(347.5)
    private val SELL = BigDecimal.valueOf(365.5)
    private const val SOURCE = "oficial"
    private const val NAME = "Oficial"
    private const val CURRENCY_CODE = "USD"
    private val CURRENCY = Currency.getInstance(CURRENCY_CODE)
    private val UPDATED_AT = ZonedDateTime.parse("2023-11-06T17:48:00Z")

    fun exchangeRateControllerResponse(): ExchangeRateControllerResponse =
        ExchangeRateControllerResponse(
            buy = BUY,
            sell = SELL,
            source = SOURCE,
            name = NAME,
            currency = CURRENCY_CODE,
            updatedAt = UPDATED_AT
        )

    fun exchangeRate(): ExchangeRate =
        ExchangeRate(
            buy = BUY,
            sell = SELL,
            source = SOURCE,
            name = NAME,
            currency = CURRENCY,
            updatedAt = UPDATED_AT
        )

    fun exchangeRateRestResponse(): ExchangeRateRestResponse =
        ExchangeRateRestResponse(
            buy = BUY,
            sell = SELL,
            source = SOURCE,
            name = NAME,
            currency = CURRENCY_CODE,
            updatedAt = UPDATED_AT,
        )

}
