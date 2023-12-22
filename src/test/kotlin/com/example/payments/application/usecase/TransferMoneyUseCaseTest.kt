package com.example.payments.application.usecase

import com.example.payments.application.port.out.AccountLockPort
import com.example.payments.application.port.out.LoadAccountPort
import com.example.payments.application.port.out.UpdateAccountStatePort
import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.BusinessException
import com.example.payments.config.exception.NotFoundException
import com.example.payments.domain.Money
import com.example.payments.extension.isCloseTo
import com.example.payments.mock.AccountMockFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.argThat
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class TransferMoneyUseCaseTest {

    private val accountLockPort = Mockito.mock(AccountLockPort::class.java)
    private val loadAccountPort = Mockito.mock(LoadAccountPort::class.java)
    private val updateAccountStatePort = Mockito.mock(UpdateAccountStatePort::class.java)

    private var useCase: TransferMoneyUseCase? = null

    private fun getBaselineDate() = LocalDateTime.now().minusDays(10L)

    @BeforeEach
    fun setup() {
        useCase = TransferMoneyUseCase(
            accountLockPort = accountLockPort,
            loadAccountPort = loadAccountPort,
            updateAccountStatePort = updateAccountStatePort
        )
    }

    @Test
    fun willThrowBusinessException_WhenSourceAccountAndTargetAccountAreTheSame() {
        val cmd = AccountMockFactory.transferMoneyOperationCommand()
        val actual = useCase!!.execute(cmd.copy(targetAccount = cmd.sourceAccount))

        StepVerifier.create(actual)
            .expectErrorMatches { it is BusinessException && it.errorCode == ErrorCode.SAME_ACCOUNTS }
            .verify()
    }

    @Test
    fun willThrowNotFound_WhenSourceAccountIsNotFound() {
        val cmd = AccountMockFactory.transferMoneyOperationCommand()
        val expectedBaselineDate = getBaselineDate()

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.error(NotFoundException(ErrorCode.RESOURCE_NOT_FOUND)))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.targetAccount()))

        val actual = useCase!!.execute(cmd)

        StepVerifier.create(actual)
            .expectErrorMatches { it is NotFoundException && it.errorCode == ErrorCode.RESOURCE_NOT_FOUND }
            .verify()

        Mockito.verify(loadAccountPort, Mockito.times(1))
            .loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
    }

    @Test
    fun willThrowNotFound_WhenTargetAccountIsNotFound() {
        val cmd = AccountMockFactory.transferMoneyOperationCommand()
        val expectedBaselineDate = getBaselineDate()

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.sourceAccount()))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.error(NotFoundException(ErrorCode.RESOURCE_NOT_FOUND)))

        val actual = useCase!!.execute(cmd)

        StepVerifier.create(actual)
            .expectErrorMatches { it is NotFoundException && it.errorCode == ErrorCode.RESOURCE_NOT_FOUND }
            .verify()

        Mockito.verify(loadAccountPort, Mockito.times(1))
            .loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
    }

    @Test
    fun willThrowBusinessException_WhenSourceAccountIsNotAvailable() {
        val cmd = AccountMockFactory.transferMoneyOperationCommand()
        val expectedBaselineDate = getBaselineDate()

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.sourceAccount()))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.targetAccount()))

        Mockito.`when`(accountLockPort.lockAccount(cmd.sourceAccount)).thenReturn(false)

        val actual = useCase!!.execute(cmd)

        StepVerifier.create(actual)
            .expectErrorMatches { it is BusinessException && it.errorCode == ErrorCode.ACCOUNT_NOT_AVAILABLE }
            .verify()

        Mockito.verify(accountLockPort, Mockito.times(1)).lockAccount(cmd.sourceAccount)
        Mockito.verify(accountLockPort, Mockito.never()).lockAccount(cmd.targetAccount)
    }

    @Test
    fun willThrowBusinessException_WhenSourceAccountDoesNotHaveEnoughFunds() {
        val expectedBaselineDate = getBaselineDate()
        val sourceAccount = AccountMockFactory.sourceAccount()
        val cmd = AccountMockFactory.transferMoneyOperationCommand().copy(amount = Money.of(Long.MAX_VALUE))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(sourceAccount))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.targetAccount()))

        Mockito.`when`(accountLockPort.lockAccount(cmd.sourceAccount)).thenReturn(true)

        val actual = useCase!!.execute(cmd)

        StepVerifier.create(actual)
            .expectErrorMatches { it is BusinessException && it.errorCode == ErrorCode.INSUFFICIENT_FUNDS }
            .verify()

        Mockito.verify(accountLockPort, Mockito.times(1)).lockAccount(cmd.sourceAccount)
        Mockito.verify(accountLockPort, Mockito.never()).lockAccount(cmd.targetAccount)
        Mockito.verify(accountLockPort, Mockito.times(1)).releaseAccount(cmd.sourceAccount)
    }

    @Test
    fun willThrowBusinessException_WhenTargetAccountIsNotAvailable() {
        val cmd = AccountMockFactory.transferMoneyOperationCommand()
        val expectedBaselineDate = getBaselineDate()

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.sourceAccount()))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(AccountMockFactory.targetAccount()))

        Mockito.`when`(accountLockPort.lockAccount(cmd.sourceAccount)).thenReturn(true)
        Mockito.`when`(accountLockPort.lockAccount(cmd.targetAccount)).thenReturn(false)

        val actual = useCase!!.execute(cmd)

        StepVerifier.create(actual)
            .expectErrorMatches { it is BusinessException && it.errorCode == ErrorCode.ACCOUNT_NOT_AVAILABLE }
            .verify()

        Mockito.verify(accountLockPort, Mockito.times(1)).lockAccount(cmd.sourceAccount)
        Mockito.verify(accountLockPort, Mockito.times(1)).lockAccount(cmd.targetAccount)
        Mockito.verify(accountLockPort, Mockito.times(1)).releaseAccount(cmd.sourceAccount)
    }

    @Test
    fun willSaveTheNewActivitiesInBothAccounts_WhenTheTransferIsComplete() {
        val cmd = AccountMockFactory.transferMoneyOperationCommand()
        val expectedBaselineDate = getBaselineDate()
        val sourceAccount = AccountMockFactory.sourceAccount()
        val targetAccount = AccountMockFactory.targetAccount()

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.sourceAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(sourceAccount))

        Mockito.`when`(
            loadAccountPort.loadAccount(
                Mockito.eq(cmd.targetAccount),
                argThat { date -> date.isCloseTo(expectedBaselineDate) }
            )
        ).thenReturn(Mono.just(targetAccount))

        Mockito.`when`(accountLockPort.lockAccount(cmd.sourceAccount)).thenReturn(true)
        Mockito.`when`(accountLockPort.lockAccount(cmd.targetAccount)).thenReturn(true)

        Mockito.`when`(updateAccountStatePort.updateActivities(sourceAccount)).thenReturn(Mono.just(Unit))
        Mockito.`when`(updateAccountStatePort.updateActivities(targetAccount)).thenReturn(Mono.just(Unit))

        val actual = useCase!!.execute(cmd)

        StepVerifier.create(actual)
            .expectNext(Unit)
            .verifyComplete()

        Mockito.verify(accountLockPort, Mockito.times(1)).lockAccount(cmd.sourceAccount)
        Mockito.verify(accountLockPort, Mockito.times(1)).lockAccount(cmd.targetAccount)
        Mockito.verify(accountLockPort, Mockito.times(1)).releaseAccount(cmd.sourceAccount)
        Mockito.verify(accountLockPort, Mockito.times(1)).releaseAccount(cmd.targetAccount)
    }

}
