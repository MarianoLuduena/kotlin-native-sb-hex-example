package com.example.payments.adapter.controller.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TransferMoneyControllerResponse(
    val sourceAccount: Long,
    val destinationAccount: Long,
    val amount: Long
)
