package com.example.payments.application.port.out

import com.example.payments.domain.Account
import reactor.core.publisher.Mono

interface UpdateAccountStatePort {

    fun updateActivities(account: Account): Mono<Unit>

}
