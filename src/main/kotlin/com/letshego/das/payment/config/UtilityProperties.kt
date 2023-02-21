package com.letshego.das.payment.config

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.TransactionAction
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "vas")
class UtilityProperties{
    lateinit var service: Map<CountryCodes, Map<String, String>>
    lateinit var utility : Map<CountryCodes, Map<TransactionAction, String>>
    lateinit var productId : Map<CountryCodes, Map<String,Map<String,String>>>
}