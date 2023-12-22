package com.example.payments.config

class ErrorCode(val code: String, val reasonPhrase: String) {

    companion object {
        val INTERNAL_ERROR = ErrorCode("101", "Internal server error")
        val BAD_REQUEST = ErrorCode("102", "Bad request")
        val RESOURCE_NOT_FOUND = ErrorCode("103", "Resource not found")
        val ACCOUNT_NOT_AVAILABLE = ErrorCode("104", "Another operation is already in progress")
        val INSUFFICIENT_FUNDS = ErrorCode("105", "Not enough funds for the transfer")
        val SAME_ACCOUNTS = ErrorCode("106", "Source and target accounts cannot be the same")
    }

}
