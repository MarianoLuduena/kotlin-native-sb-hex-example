package com.example.payments.config.http

import com.example.payments.extension.sanitized
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpResponse
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.function.Function

@Configuration
class HttpConfig {

    private val requestLogger = LoggerFactory.getLogger("HttpRequestLogger")
    private val responseLogger = LoggerFactory.getLogger("HttpResponseLogger")

    @Bean
    @RegisterReflectionForBinding(ErrorBody::class)
    fun getWebClient(
        @Value("\${http.client.connect-timeout}") connectTimeout: Int,
        @Value("\${http.client.read-timeout}") readTimeout: Int,
        objectMapper: ObjectMapper,
        webClientBuilder: WebClient.Builder
    ): WebClient {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout.toLong()))

        return webClientBuilder
            .clientConnector(buildClientConnector(httpClient))
            .filter(ExchangeFilterFunction.ofRequestProcessor(logRequest()))
            .filter(ExchangeFilterFunction.ofResponseProcessor(logResponse()))
            .filter(ExchangeFilterFunction.ofResponseProcessor(WebClientErrorHandler(objectMapper)::handle))
            .build()
    }

    private fun buildClientConnector(httpClient: HttpClient): ClientHttpConnector =
        object : ReactorClientHttpConnector(httpClient) {
            override fun connect(
                method: HttpMethod,
                uri: URI,
                requestCallback: Function<in ClientHttpRequest, Mono<Void>>
            ): Mono<ClientHttpResponse> {
                return super.connect(
                    method,
                    uri
                ) { request ->
                    request.headers.contentLength
                        .takeIf { it > 0L }
                        ?.let { requestCallback.apply(LoggingClientHttpRequestDecorator(request, requestLogger)) }
                        ?: requestCallback.apply(request)
                }
            }
        }

    private fun logRequest(): (ClientRequest) -> Mono<ClientRequest> {
        return { request: ClientRequest ->
            request.headers().contentLength
                .takeIf { it <= 0L }
                ?.let {
                    requestLogger.info("{} {} | {}", request.method(), request.url(), request.headers().sanitized())
                }
            Mono.just(request)
        }
    }

    private fun logResponse(): (ClientResponse) -> Mono<ClientResponse> {
        return { response: ClientResponse ->
            DataBufferUtils.join(response.body(BodyExtractors.toDataBuffers()))
                .map { dataBuffer ->
                    val httpStatus = HttpStatus.valueOf(response.statusCode().value())
                    val headers = response.headers().asHttpHeaders()
                    val body: String = dataBuffer.toString(StandardCharsets.UTF_8)
                    val bodyToLog =
                        if (MediaType.APPLICATION_OCTET_STREAM == headers.getContentType()) {
                            "[" + headers.getContentLength() + " bytes]"
                        } else {
                            body
                        }

                    responseLogger.info(
                        "{} - {} | {} | Response: {}",
                        httpStatus.value(),
                        httpStatus.reasonPhrase,
                        headers,
                        bodyToLog
                    )

                    response.mutate().body(body).build()
                }
        }
    }

}
