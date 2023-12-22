package com.example.payments.config.exception

import com.example.payments.config.ErrorCode
import java.lang.RuntimeException

open class GenericException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.reasonPhrase)
