package com.example.payments.extension

import org.springframework.http.HttpHeaders

fun HttpHeaders.sanitized(): Map<String, List<String>> =
    this.mapValues { entry ->
        entry
            .takeIf { HttpHeaders.AUTHORIZATION.equals(entry.key, ignoreCase = true) }
            ?.let {
                val value = it.value.first().take(12) + "..."
                listOf(value)
            }
            ?: entry.value
    }
