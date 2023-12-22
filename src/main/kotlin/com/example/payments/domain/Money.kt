package com.example.payments.domain

import java.math.BigInteger

data class Money(val amount: BigInteger) {

    fun isPositiveOrZero(): Boolean = amount >= BigInteger.ZERO

//    fun isNegative(): Boolean = amount < BigInteger.ZERO
//
//    fun isPositive(): Boolean = amount > BigInteger.ZERO
//
//    fun isGreaterThanOrEqualTo(money: Money): Boolean = amount >= money.amount
//
//    fun isGreaterThan(money: Money): Boolean = amount > money.amount

    fun minus(money: Money): Money = Money(amount.subtract(money.amount))

    fun plus(money: Money): Money = Money(amount.add(money.amount))

    companion object {
        val ZERO = of(0L)
        fun of(value: Long): Money = Money(BigInteger.valueOf(value))
    }

}
