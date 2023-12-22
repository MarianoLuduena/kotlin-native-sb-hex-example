package com.example.payments.adapter.persistence.repository

import com.example.payments.adapter.persistence.model.ActivityTableModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface ActivityRepository : ReactiveCrudRepository<ActivityTableModel, Long> {

    fun findByOwnerAccountIdAndTimestampIsGreaterThanEqual(
        ownerAccountId: Long,
        since: LocalDateTime
    ): Flux<ActivityTableModel>

    @Query(
        "SELECT SUM(amount) " +
                "FROM activity " +
                "WHERE target_account_id = :accountId " +
                "AND owner_account_id = :accountId " +
                "AND created_at < :until"
    )
    fun getDepositBalanceUntil(accountId: Long, until: LocalDateTime): Mono<Long>

    @Query(
        "SELECT SUM(amount) " +
                "FROM activity " +
                "WHERE source_account_id = :accountId " +
                "AND owner_account_id = :accountId " +
                "AND created_at < :until"
    )
    fun getWithdrawalBalanceUntil(accountId: Long, until: LocalDateTime): Mono<Long>

}
