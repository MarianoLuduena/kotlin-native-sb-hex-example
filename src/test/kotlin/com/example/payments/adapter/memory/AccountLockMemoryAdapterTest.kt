package com.example.payments.adapter.memory

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccountLockMemoryAdapterTest {

    private var adapter: AccountLockMemoryAdapter? = null

    @BeforeEach
    fun setup() {
        adapter = AccountLockMemoryAdapter()
    }

    @Test
    fun willReturnTrue_WhenALockIsObtainedOnANewlyCreatedAdapter() {
        Assertions.assertThat(adapter!!.lockAccount(ACCOUNT_ID)).isTrue()
    }

    @Test
    fun willReturnFalse_WhenAttemptingToLockTheSameAccountASecondTime() {
        Assertions.assertThat(adapter!!.lockAccount(ACCOUNT_ID)).isTrue()
        Assertions.assertThat(adapter!!.lockAccount(ACCOUNT_ID)).isFalse()
    }

    @Test
    fun willReturnTrue_WhenLockingAnAccountReleasingItAndLockingItAgain() {
        Assertions.assertThat(adapter!!.lockAccount(ACCOUNT_ID)).isTrue()
        adapter!!.releaseAccount(ACCOUNT_ID)
        Assertions.assertThat(adapter!!.lockAccount(ACCOUNT_ID)).isTrue()
    }

    companion object {
        private const val ACCOUNT_ID = 31415L
    }

}
