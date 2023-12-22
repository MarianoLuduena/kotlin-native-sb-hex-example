package com.example.payments.domain

data class ActivityWindow(
    private val activities: MutableList<Activity>
) {

    fun getActivities(): List<Activity> = activities.toList()

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    /**
     * Calculates the balance by summing up the values of all activities within this window.
     */
    fun calculateBalance(accountId: Long): Money {
        val depositBalance =
            activities
                .filter { it.targetAccountId == accountId }
                .map { it.amount }
                .fold(Money.ZERO) { acc, money -> acc.plus(money) }

        val withdrawalBalance =
            activities
                .filter { it.sourceAccountId == accountId }
                .map { it.amount }
                .fold(Money.ZERO) { acc, money -> acc.plus(money) }

        return depositBalance.minus(withdrawalBalance)
    }

}
