package com.example.payments.extension

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType

fun MockWebServer.mockJsonResponse(
    responseBody: String,
    responseStatusCode: HttpStatusCode = HttpStatus.OK
): Unit =
    this.enqueue(
        MockResponse()
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(responseBody)
            .setResponseCode(responseStatusCode.value())
    )
