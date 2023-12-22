package com.example.payments.adapter.persistence

import com.example.payments.adapter.persistence.mapper.AccountMapper
import com.example.payments.adapter.persistence.model.ActivityTableModel
import com.example.payments.adapter.persistence.repository.AccountRepository
import com.example.payments.adapter.persistence.repository.ActivityRepository
import com.example.payments.application.port.out.LoadAccountPort
import com.example.payments.application.port.out.UpdateAccountStatePort
import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.NotFoundException
import com.example.payments.domain.Account
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.kotlin.core.util.function.component4
import java.time.LocalDateTime

@Repository
class AccountPersistenceAdapter(
    private val accountRepository: AccountRepository,
    private val activityRepository: ActivityRepository,
    private val accountMapper: AccountMapper
) : LoadAccountPort, UpdateAccountStatePort {

    override fun loadAccount(accountId: Long, baselineDate: LocalDateTime): Mono<Account> {
        LOG.info("Loading account {} with baselineDate {}", accountId, baselineDate)

        val mAccount =
            accountRepository.findById(accountId)
                .switchIfEmpty { Mono.error(NotFoundException(ErrorCode.RESOURCE_NOT_FOUND)) }

        val mActivities =
            activityRepository.findByOwnerAccountIdAndTimestampIsGreaterThanEqual(accountId, baselineDate)
                .collectList()

        val mDepositBalance =
            activityRepository.getDepositBalanceUntil(accountId, baselineDate)
                .defaultIfEmpty(0L)

        val mWithdrawalBalance =
            activityRepository.getWithdrawalBalanceUntil(accountId, baselineDate)
                .defaultIfEmpty(0L)

        return Mono.zip(mAccount, mActivities, mDepositBalance, mWithdrawalBalance)
            .map { (account, activities, depositBalance, withdrawalBalance) ->
                LOG.info(
                    "Loaded account {} with withdrawal balance {} and deposit balance {}",
                    account, withdrawalBalance, depositBalance
                )
                accountMapper.toDomain(account, activities, withdrawalBalance, depositBalance)
            }
    }

    override fun updateActivities(account: Account): Mono<Unit> =
        account.activityWindow.getActivities()
            .filter { it.id == null }
            .map { ActivityTableModel.from(it) }
            .let { activitiesToSave ->
                LOG.info("Saving activities {}", activitiesToSave)
                activityRepository.saveAll(activitiesToSave)
            }
            .collectList()
            .then(Mono.empty())

    companion object {
        private val LOG = LoggerFactory.getLogger(AccountPersistenceAdapter::class.java)
    }

}
