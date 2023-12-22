package com.example.payments.adapter.persistence.mapper

import com.example.payments.adapter.persistence.model.AccountTableModel
import com.example.payments.adapter.persistence.model.ActivityTableModel
import com.example.payments.domain.Account
import com.example.payments.domain.Activity
import com.example.payments.domain.ActivityWindow
import com.example.payments.domain.Money
import org.springframework.stereotype.Component

@Component
class AccountMapper {

    fun toDomain(
        account: AccountTableModel,
        activities: List<ActivityTableModel>,
        withdrawalBalance: Long,
        depositBalance: Long
    ): Account {
        val baselineBalance = Money.of(depositBalance).minus(Money.of(withdrawalBalance))
        return Account(
            id = account.id!!,
            activityWindow = toDomain(activities),
            baselineBalance = baselineBalance
        )
    }

    private fun toDomain(activities: List<ActivityTableModel>): ActivityWindow =
        mutableListOf<Activity>()
            .let { domainActivities ->
                activities.forEach { domainActivities.add(it.toDomain()) }
                ActivityWindow(domainActivities)
            }

}
