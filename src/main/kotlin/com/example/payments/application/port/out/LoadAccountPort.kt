package com.example.payments.application.port.out

import com.example.payments.domain.Account
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface LoadAccountPort {

    fun loadAccount(accountId: Long, baselineDate: LocalDateTime): Mono<Account>

}
