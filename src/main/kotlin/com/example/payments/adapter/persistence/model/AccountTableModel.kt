package com.example.payments.adapter.persistence.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("account")
data class AccountTableModel(
    @Id val id: Long?,
    val currency: String,
    @Column("created_at") val timestamp: LocalDateTime
)
