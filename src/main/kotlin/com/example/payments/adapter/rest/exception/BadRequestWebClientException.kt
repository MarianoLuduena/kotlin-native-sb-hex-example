package com.example.payments.adapter.rest.exception

import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.GenericException

class BadRequestWebClientException(errorCode: ErrorCode) : GenericException(errorCode)
