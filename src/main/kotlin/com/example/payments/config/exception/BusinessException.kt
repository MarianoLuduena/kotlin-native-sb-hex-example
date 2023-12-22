package com.example.payments.config.exception

import com.example.payments.config.ErrorCode

class BusinessException(errorCode: ErrorCode) : GenericException(errorCode)
