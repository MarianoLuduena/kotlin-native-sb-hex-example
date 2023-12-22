package com.example.payments.config

object TestConfig {

    fun config(): Config = Config(exchangeRateOutPort = Config.RestPort("mock"))

}
