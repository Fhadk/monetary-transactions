package com.letshego.das.payment

import com.letshego.das.payment.config.PaymentErrorProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ContextConfiguration(classes = [(EWalletServiceApplication::class)])
class PaymentConfigTest {

    @Autowired
    lateinit var properties: PaymentErrorProperties

    @Test
    fun testLoadingPropertiesTest(){
        assertNotNull(properties.errors)
    }

    @Test
    fun checkMappingForWalletMaxLimitTest(){
        assertEquals("Wallet balance max limit exceeded", properties.errors["122001"])
    }
}