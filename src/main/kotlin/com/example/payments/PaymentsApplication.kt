package com.example.payments

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
class PaymentsApplication

fun main(args: Array<String>) {
	Hooks.enableAutomaticContextPropagation()
	runApplication<PaymentsApplication>(*args)
}
