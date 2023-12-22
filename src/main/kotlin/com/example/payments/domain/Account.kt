package com.example.payments.domain

import java.time.LocalDateTime

data class Account(
    val id: Long,
    val baselineBalance: Money,
    val activityWindow: ActivityWindow
) {

    /**
     * Calculates the total balance of the account by adding the activity values to the baseline balance.
     */
    private fun calculateBalance(): Money = baselineBalance.plus(activityWindow.calculateBalance(id))

    /**
     * Tries to withdraw a certain amount of money from this account.
     * If successful, creates a new activity with a negative value.
     *
     * @return true if the withdrawal was successful, false if not.
     */
    fun withdraw(money: Money, targetAccountId: Long): Boolean {
        if (!mayWithdraw(money)) {
            return false
        }

        val withdrawal = Activity.of(id, id, targetAccountId, LocalDateTime.now(), money)
        activityWindow.addActivity(withdrawal)
        return true
    }

    private fun mayWithdraw(money: Money): Boolean = calculateBalance().minus(money).isPositiveOrZero()

    /**
     * Deposit a certain amount of money to this account.
     * Creates a new activity with a positive value.
     */
    fun deposit(money: Money, sourceAccountId: Long) {
        val deposit = Activity.of(id, sourceAccountId, id, LocalDateTime.now(), money)
        activityWindow.addActivity(deposit)
    }

}
