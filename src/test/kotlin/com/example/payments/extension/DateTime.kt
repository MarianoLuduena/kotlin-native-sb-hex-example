package com.example.payments.extension

import java.time.LocalDateTime

fun LocalDateTime.isCloseTo(that: LocalDateTime, marginInSeconds: Long = 5L): Boolean =
    this.isBefore(that.plusSeconds(marginInSeconds))
            && this.plusSeconds(marginInSeconds).isAfter(that)
