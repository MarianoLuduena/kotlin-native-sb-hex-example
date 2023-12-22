package com.example.payments.adapter.persistence

import com.example.payments.adapter.persistence.mapper.AccountMapper
import com.example.payments.adapter.persistence.repository.AccountRepository
import com.example.payments.adapter.persistence.repository.ActivityRepository
import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.NotFoundException
import com.example.payments.mock.AccountMockFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class AccountPersistenceAdapterTest {

    private val accountRepository = Mockito.mock(AccountRepository::class.java)
    private val activityRepository = Mockito.mock(ActivityRepository::class.java)
    private val accountMapper = AccountMapper()

    private var adapter: AccountPersistenceAdapter? = null

    @BeforeEach
    fun setup() {
        adapter = AccountPersistenceAdapter(accountRepository, activityRepository, accountMapper)
    }

    @Test
    fun willThrowNotFoundException_WhenLoadAccountIsUnableToFindTheAccount() {
        Mockito.`when`(accountRepository.findById(ACCOUNT_ID))
            .thenReturn(Mono.empty())

        Mockito.`when`(activityRepository.findByOwnerAccountIdAndTimestampIsGreaterThanEqual(ACCOUNT_ID, BASELINE_DATE))
            .thenReturn(Flux.empty())

        Mockito.`when`(activityRepository.getDepositBalanceUntil(ACCOUNT_ID, BASELINE_DATE)).thenReturn(Mono.empty())

        Mockito.`when`(activityRepository.getWithdrawalBalanceUntil(ACCOUNT_ID, BASELINE_DATE)).thenReturn(Mono.empty())

        val actual = adapter!!.loadAccount(ACCOUNT_ID, BASELINE_DATE)

        StepVerifier.create(actual)
            .expectErrorMatches { it is NotFoundException && it.errorCode == ErrorCode.RESOURCE_NOT_FOUND }
            .verify()
    }

    @Test
    fun willReturnAnAccountWithPrecalculatedBalance_WhenLoadAccountIsCalled() {
        val expected = AccountMockFactory.sourceAccount()

        Mockito.`when`(accountRepository.findById(ACCOUNT_ID))
            .thenReturn(Mono.just(AccountMockFactory.sourceAccountTableModel()))

        Mockito.`when`(activityRepository.findByOwnerAccountIdAndTimestampIsGreaterThanEqual(ACCOUNT_ID, BASELINE_DATE))
            .thenReturn(Flux.just(AccountMockFactory.savedSourceAccountActivityTableModel()))

        Mockito.`when`(activityRepository.getDepositBalanceUntil(ACCOUNT_ID, BASELINE_DATE))
            .thenReturn(Mono.just(DEPOSIT_BALANCE))

        Mockito.`when`(activityRepository.getWithdrawalBalanceUntil(ACCOUNT_ID, BASELINE_DATE))
            .thenReturn(Mono.just(WITHDRAWAL_BALANCE))

        val actual = adapter!!.loadAccount(ACCOUNT_ID, BASELINE_DATE)

        StepVerifier.create(actual)
            .expectNext(expected)
            .verifyComplete()
    }

    @Test
    fun willSaveOnlyNewActivities_WhenUpdateActivitiesIsCalled() {
        val account = AccountMockFactory.sourceAccountWithTwoActivities()

        Mockito.`when`(activityRepository.saveAll(listOf(AccountMockFactory.sourceAccountActivityTableModelToSave())))
            .thenReturn(Flux.just(AccountMockFactory.savedSourceAccountActivityTableModel()))

        StepVerifier.create(adapter!!.updateActivities(account))
            .verifyComplete()
    }

    companion object {
        private const val ACCOUNT_ID = 42042L
        private const val DEPOSIT_BALANCE = 24000L
        private const val WITHDRAWAL_BALANCE = 4000L
        private val BASELINE_DATE = LocalDateTime.of(2023, 11, 14, 0, 0, 0)
    }

}
