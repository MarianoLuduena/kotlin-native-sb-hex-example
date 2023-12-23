package com.example.payments.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("my-app")
data class Config(
    @NestedConfigurationProperty val exchangeRateOutPort: RestPort
) {

    data class RestPort(
        @get:NotBlank val url: String
    )

}
