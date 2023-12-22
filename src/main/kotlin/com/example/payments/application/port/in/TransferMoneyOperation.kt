package com.example.payments.application.port.`in`

import com.example.payments.domain.Money
import reactor.core.publisher.Mono

interface TransferMoneyOperation {

    fun execute(cmd: Command): Mono<Unit>

    data class Command(
        val sourceAccount: Long,
        val targetAccount: Long,
        val amount: Money
    )

}
