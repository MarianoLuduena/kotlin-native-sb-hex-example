package com.example.payments.adapter.controller.model

import com.example.payments.application.port.`in`.TransferMoneyOperation
import com.example.payments.domain.Money
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.Positive

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TransferMoneyControllerRequest(
    @Positive val targetAccount: Long,
    @Positive val amount: Long
) {

    fun toDomain(sourceAccount: Long): TransferMoneyOperation.Command =
        TransferMoneyOperation.Command(
            sourceAccount = sourceAccount,
            targetAccount = targetAccount,
            amount = Money.of(amount)
        )

}
