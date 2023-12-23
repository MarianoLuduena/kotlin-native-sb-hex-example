package com.example.payments.adapter.rest

import com.example.payments.adapter.rest.model.ExchangeRateRestResponse
import com.example.payments.application.port.out.ExchangeRatesPort
import com.example.payments.config.Config
import com.example.payments.domain.ExchangeRate
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Repository
class ExchangeRateRestAdapter(
    private val webClient: WebClient,
    private val config: Config
) : ExchangeRatesPort {

    @RegisterReflectionForBinding(ExchangeRateRestResponse::class)
    override fun get(): Flux<ExchangeRate> {
        LOG.info("Getting exchange rates from remote API")
        return webClient
            .get()
            .uri(config.exchangeRateOutPort.url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(ExchangeRateRestResponse::class.java)
            .map { response ->
                LOG.info("Got response {}", response)
                response.toDomain()
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ExchangeRateRestAdapter::class.java)
    }

}
