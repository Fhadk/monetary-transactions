package com.letshego.das.payment.config

import com.letshego.das.ms.CountryCodes
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
@ConfigurationProperties(prefix = "payment")
class PaymentProperties {

    lateinit var config: Map<CountryCodes, PaymentConfig>

    class PaymentConfig {
        var transaction: TransactionLimit = TransactionLimit()
        var monthly: TransactionLimit = TransactionLimit()
        lateinit var currencyCode: String

    }

    class TransactionLimit {
        lateinit var limit: MutableMap<String, BigDecimal>
    }

}


@Configuration
@ConfigurationProperties(prefix = "mobifin-mapping")
class PaymentErrorProperties {
    lateinit var errors: Map<String, String>
}