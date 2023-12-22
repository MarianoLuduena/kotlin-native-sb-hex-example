package com.example.payments.adapter.memory

import com.example.payments.application.port.out.AccountLockPort
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class AccountLockMemoryAdapter : AccountLockPort {

    private val lockedAccounts = ConcurrentHashMap<Long, Boolean>()

    override fun lockAccount(accountId: Long): Boolean = lockedAccounts.putIfAbsent(accountId, false) ?: true

    override fun releaseAccount(accountId: Long) {
        lockedAccounts.remove(accountId)
    }

}
