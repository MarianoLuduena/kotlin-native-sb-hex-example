package com.example.payments.mock

import com.example.payments.adapter.controller.model.TransferMoneyControllerRequest
import com.example.payments.adapter.controller.model.TransferMoneyControllerResponse
import com.example.payments.adapter.persistence.model.AccountTableModel
import com.example.payments.adapter.persistence.model.ActivityTableModel
import com.example.payments.application.port.`in`.TransferMoneyOperation
import com.example.payments.domain.Account
import com.example.payments.domain.Activity
import com.example.payments.domain.ActivityWindow
import com.example.payments.domain.Money
import java.time.LocalDateTime
import java.util.Currency

object AccountMockFactory {

    private const val SOURCE_ACCOUNT = 42042L
    private const val SOURCE_ACCOUNT_ACTIVITY_ID = 717882L
    private const val TARGET_ACCOUNT = 98132L
    private const val AMOUNT_TO_TRANSFER = 6800L
    private const val AMOUNT_PREVIOUSLY_TRANSFERRED = 3200L
    private const val SOURCE_ACCOUNT_BASELINE_AMOUNT = 20000L
    private const val TARGET_ACCOUNT_BASELINE_AMOUNT = 15000L
    private val ACTIVITY_TIMESTAMP = LocalDateTime.of(2023, 11, 14, 12, 0, 0)
    private val ACCOUNT_CREATED_AT = LocalDateTime.of(2023, 5, 13, 15, 1, 7)
    private val USD_CURRENCY = Currency.getInstance("USD")

    fun transferMoneyControllerRequest(): TransferMoneyControllerRequest =
        TransferMoneyControllerRequest(
            targetAccount = TARGET_ACCOUNT,
            amount = AMOUNT_TO_TRANSFER
        )

    fun transferMoneyControllerResponse(): TransferMoneyControllerResponse =
        TransferMoneyControllerResponse(
            sourceAccount = SOURCE_ACCOUNT,
            destinationAccount = TARGET_ACCOUNT,
            amount = AMOUNT_TO_TRANSFER
        )

    fun transferMoneyOperationCommand(): TransferMoneyOperation.Command =
        TransferMoneyOperation.Command(
            sourceAccount = SOURCE_ACCOUNT,
            targetAccount = TARGET_ACCOUNT,
            amount = Money.of(AMOUNT_TO_TRANSFER)
        )

    fun sourceAccount(): Account =
        Account(
            id = SOURCE_ACCOUNT,
            baselineBalance = Money.of(SOURCE_ACCOUNT_BASELINE_AMOUNT),
            activityWindow = sourceAccountActivityWindow()
        )

    fun sourceAccountWithTwoActivities(): Account =
        Account(
            id = SOURCE_ACCOUNT,
            baselineBalance = Money.of(SOURCE_ACCOUNT_BASELINE_AMOUNT),
            activityWindow = sourceAccountActivityWindowWithTwoActivities()
        )

    fun targetAccount(): Account =
        Account(
            id = TARGET_ACCOUNT,
            baselineBalance = Money.of(TARGET_ACCOUNT_BASELINE_AMOUNT),
            activityWindow = targetAccountActivityWindow()
        )

    private fun sourceAccountActivityWindow(): ActivityWindow =
        ActivityWindow(activities = mutableListOf(sourceAccountActivity()))

    private fun sourceAccountActivityWindowWithTwoActivities(): ActivityWindow =
        ActivityWindow(
            activities = mutableListOf(
                sourceAccountActivity(),
                sourceAccountActivity().copy(id = null)
            )
        )

    private fun sourceAccountActivity(): Activity =
        Activity(
            id = SOURCE_ACCOUNT_ACTIVITY_ID,
            ownerAccountId = SOURCE_ACCOUNT,
            sourceAccountId = SOURCE_ACCOUNT,
            targetAccountId = TARGET_ACCOUNT,
            timestamp = ACTIVITY_TIMESTAMP,
            amount = Money.of(AMOUNT_PREVIOUSLY_TRANSFERRED)
        )

    private fun targetAccountActivityWindow(): ActivityWindow =
        ActivityWindow(
            mutableListOf(
                Activity.of(
                    ownerAccountId = TARGET_ACCOUNT,
                    sourceAccountId = SOURCE_ACCOUNT,
                    targetAccountId = TARGET_ACCOUNT,
                    timestamp = ACTIVITY_TIMESTAMP,
                    amount = Money.of(AMOUNT_PREVIOUSLY_TRANSFERRED)
                )
            )
        )

    fun sourceAccountTableModel(): AccountTableModel =
        AccountTableModel(
            id = SOURCE_ACCOUNT,
            currency = USD_CURRENCY.currencyCode,
            timestamp = ACCOUNT_CREATED_AT
        )

    fun savedSourceAccountActivityTableModel(): ActivityTableModel =
        sourceActivityTableModel(SOURCE_ACCOUNT_ACTIVITY_ID)

    fun sourceAccountActivityTableModelToSave(): ActivityTableModel = sourceActivityTableModel(null)

    private fun sourceActivityTableModel(id: Long?): ActivityTableModel =
        ActivityTableModel(
            id = id,
            timestamp = ACTIVITY_TIMESTAMP,
            ownerAccountId = SOURCE_ACCOUNT,
            sourceAccountId = SOURCE_ACCOUNT,
            targetAccountId = TARGET_ACCOUNT,
            amount = AMOUNT_PREVIOUSLY_TRANSFERRED
        )

}
