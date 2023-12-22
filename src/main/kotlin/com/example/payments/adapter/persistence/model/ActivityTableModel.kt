package com.example.payments.adapter.persistence.model

import com.example.payments.domain.Activity
import com.example.payments.domain.Money
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("activity")
data class ActivityTableModel(
    @Id val id: Long?,
    @Column("created_at") val timestamp: LocalDateTime,
    @Column("owner_account_id") val ownerAccountId: Long,
    @Column("source_account_id") val sourceAccountId: Long,
    @Column("target_account_id") val targetAccountId: Long,
    val amount: Long
) {

    fun toDomain(): Activity =
        Activity(
            id = id,
            ownerAccountId = ownerAccountId,
            sourceAccountId = sourceAccountId,
            targetAccountId = targetAccountId,
            timestamp = timestamp,
            amount = Money.of(amount)
        )

    companion object {
        fun from(domain: Activity): ActivityTableModel =
            ActivityTableModel(
                id = domain.id,
                ownerAccountId = domain.ownerAccountId,
                sourceAccountId = domain.sourceAccountId,
                targetAccountId = domain.targetAccountId,
                timestamp = domain.timestamp,
                amount = domain.amount.amount.longValueExact()
            )
    }

}
