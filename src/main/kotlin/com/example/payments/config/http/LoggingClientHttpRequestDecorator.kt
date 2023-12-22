package com.example.payments.config.http

import com.example.payments.extension.sanitized
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

class LoggingClientHttpRequestDecorator(
    delegate: ClientHttpRequest,
    private val log: Logger
) : ClientHttpRequestDecorator(delegate) {

    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        val newBody =
            Flux.from(body)
                .doOnNext { content ->
                    val contentLength = headers.contentLength
                    val bodyToLog =
                        contentLength
                            .takeIf { it <= MAX_REQUEST_BODY_LENGTH_TO_LOG }
                            ?.let { content.toString(StandardCharsets.UTF_8) }
                            ?: "[${contentLength} bytes]"
                    log.info("{} {} | {} {}", method, uri, headers.sanitized(), bodyToLog)
                }
        return super.writeWith(newBody)
    }

    companion object {
        private const val MAX_REQUEST_BODY_LENGTH_TO_LOG = 4L * 1024L
    }

}
