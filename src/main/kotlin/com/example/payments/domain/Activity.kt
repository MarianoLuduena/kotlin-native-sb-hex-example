package com.example.payments.domain

import java.time.LocalDateTime

data class Activity(
    val id: Long?,
    val ownerAccountId: Long,
    val sourceAccountId: Long,
    val targetAccountId: Long,
    val timestamp: LocalDateTime,
    val amount: Money
) {
    companion object {
        fun of(
            ownerAccountId: Long,
            sourceAccountId: Long,
            targetAccountId: Long,
            timestamp: LocalDateTime,
            amount: Money
        ): Activity =
            Activity(
                id = null,
                ownerAccountId = ownerAccountId,
                sourceAccountId = sourceAccountId,
                targetAccountId = targetAccountId,
                timestamp = timestamp,
                amount = amount
            )
    }
}
