package com.example.payments.config

import io.micrometer.tracing.Tracer
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class TraceSleuthWebFilter(
    private val tracer: Tracer
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val responseHeaders: HttpHeaders = exchange.response.headers
        if (!responseHeaders.containsKey(X_B3_TRACE_ID)) {
            val traceId: String = tracer.currentSpan()?.context()?.traceId() ?: TRACE_ID_NOT_EXISTS
            val spanId: String = tracer.currentSpan()?.context()?.spanId() ?: SPAN_ID_NOT_EXISTS
            responseHeaders.set(X_B3_TRACE_ID, traceId)
            responseHeaders.set(X_B3_SPAN_ID, spanId)
        }
        return chain.filter(exchange)
    }

    companion object {
        private const val TRACE_ID_NOT_EXISTS = "Trace id not exists"
        private const val SPAN_ID_NOT_EXISTS = "Span id not exists"
        private const val X_B3_TRACE_ID = "X-B3-TraceId"
        private const val X_B3_SPAN_ID = "X-B3-SpanId"
    }

}
