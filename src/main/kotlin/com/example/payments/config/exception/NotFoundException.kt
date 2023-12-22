package com.example.payments.config.exception

import com.example.payments.config.ErrorCode

class NotFoundException(errorCode: ErrorCode) : GenericException(errorCode)
