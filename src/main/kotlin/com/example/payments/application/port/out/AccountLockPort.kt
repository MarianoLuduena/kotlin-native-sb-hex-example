package com.example.payments.application.port.out

interface AccountLockPort {

    fun lockAccount(accountId: Long): Boolean

    fun releaseAccount(accountId: Long)

}
