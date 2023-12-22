package com.example.payments.adapter.rest.exception

import com.example.payments.config.ErrorCode
import com.example.payments.config.exception.GenericException

class WebClientGenericException(errorCode: ErrorCode) : GenericException(errorCode)
