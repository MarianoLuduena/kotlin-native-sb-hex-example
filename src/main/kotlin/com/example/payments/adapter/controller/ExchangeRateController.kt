package com.example.payments.adapter.controller

import com.example.payments.adapter.controller.model.ExchangeRateControllerResponse
import com.example.payments.application.port.`in`.GetExchangeRatesOperation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/exchange-rates")
@Validated
class ExchangeRateController(
    private val getExchangeRatesOperation: GetExchangeRatesOperation
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE])
    fun get(): Flux<ExchangeRateControllerResponse> {
        LOG.info("Call to GET /exchange-rates")
        return getExchangeRatesOperation.execute()
            .map {
                val item = ExchangeRateControllerResponse.from(it)
                LOG.info("Replying to GET /exchange-rates with item {}", item)
                item
            }
            .doOnComplete { LOG.info("Successfully replied to GET /exchange-rates") }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ExchangeRateController::class.java)
    }

}
