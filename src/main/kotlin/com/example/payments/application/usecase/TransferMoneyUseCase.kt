package com.example.payments.application.usecase

import com.example.payments.application.port.`in`.TransferMoneyOperation
import com.example.payments.application.port.out.AccountLockPort
import com.example.payments.application.port.out.LoadAccountPort
import com.example.payments.application.port.out.UpdateAccountStatePort
import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.time.LocalDateTime

@Component
class TransferMoneyUseCase(
    private val accountLockPort: AccountLockPort,
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort
) : TransferMoneyOperation {

    override fun execute(cmd: TransferMoneyOperation.Command): Mono<Unit> {
        LOG.info("Executing money transference with data {}", cmd)

        if (cmd.sourceAccount == cmd.targetAccount) {
            return Mono.error(BusinessException(ErrorCode.SAME_ACCOUNTS))
        }

        val baselineDate = LocalDateTime.now().minusDays(10L)

        val mSourceAccount = loadAccountPort.loadAccount(cmd.sourceAccount, baselineDate)
        val mTargetAccount = loadAccountPort.loadAccount(cmd.targetAccount, baselineDate)

        return Mono.zip(mSourceAccount, mTargetAccount)
            .flatMap { (sourceAccount, targetAccount) ->
                LOG.info("Found source {} and target {} accounts", sourceAccount, targetAccount)
                val lockedSourceAccount = accountLockPort.lockAccount(sourceAccount.id)
                if (!lockedSourceAccount) {
                    return@flatMap Mono.error(BusinessException(ErrorCode.ACCOUNT_NOT_AVAILABLE))
                }

                if (!sourceAccount.withdraw(cmd.amount, targetAccount.id)) {
                    accountLockPort.releaseAccount(sourceAccount.id)
                    return@flatMap Mono.error(BusinessException(ErrorCode.INSUFFICIENT_FUNDS))
                }

                val lockedTargetAccount = accountLockPort.lockAccount(targetAccount.id)
                if (!lockedTargetAccount) {
                    accountLockPort.releaseAccount(sourceAccount.id)
                    return@flatMap Mono.error(BusinessException(ErrorCode.ACCOUNT_NOT_AVAILABLE))
                }

                targetAccount.deposit(cmd.amount, sourceAccount.id)

                updateAccountStatePort.updateActivities(sourceAccount)
                    .zipWith(updateAccountStatePort.updateActivities(targetAccount))
                    .doFinally {
                        LOG.info("Releasing accounts {} and {}", sourceAccount.id, targetAccount.id)
                        accountLockPort.releaseAccount(sourceAccount.id)
                        accountLockPort.releaseAccount(targetAccount.id)
                        LOG.info("Released accounts {} and {}", sourceAccount.id, targetAccount.id)
                    }
            }
            .thenReturn(Unit)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TransferMoneyUseCase::class.java)
    }

}
