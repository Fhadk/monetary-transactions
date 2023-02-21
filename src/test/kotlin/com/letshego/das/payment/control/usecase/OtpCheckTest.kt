package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.dto.OtpCheckRequestDTO
import com.nhaarman.mockitokotlin2.any
import org.junit.Before
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class OtpCheckTest {

    private lateinit var otpCheck: OtpCheck

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @BeforeEach
    fun init() {
        otpCheck = OtpCheck(transactionsRepository, TestUtil.getPaymentProperties())
    }

    @Test
    fun `should send OTP required if first transaction`() {
        Mockito.`when`(transactionsRepository.findByCustomerIdAndCreatedDateGreaterThan(any(), any()))
                .thenReturn(emptyList())
        val res = otpCheck.invoke(getReq())
        assertNotNull(res)
        assertTrue(res.otpRequired)
    }

    @Test
    fun `should send OTP required if transaction greater than limit`() {
        Mockito.`when`(transactionsRepository.findByCustomerIdAndCreatedDateGreaterThan(any(), any()))
                .thenReturn(listOf(TestUtil.getTransaction(BigDecimal.valueOf(2000))))
        val res = otpCheck.invoke(getReq(BigDecimal.valueOf(2000)))
        assertNotNull(res)
        assertTrue(res.otpRequired)
    }

    @Test
    fun `should send OTP required if transaction greater than monthly limit`() {
        Mockito.`when`(transactionsRepository.findByCustomerIdAndCreatedDateGreaterThan(any(), any()))
                .thenReturn(listOf(TestUtil.getTransaction(BigDecimal.valueOf(2000))))
        val res = otpCheck.invoke(getReq(BigDecimal.valueOf(1200)))
        assertNotNull(res)
        assertTrue(res.otpRequired)
    }

    @Test
    fun `should not require OTP if transaction within monthly limit`() {
        Mockito.`when`(transactionsRepository.findByCustomerIdAndCreatedDateGreaterThan(any(), any()))
                .thenReturn(listOf(TestUtil.getTransaction(BigDecimal.valueOf(2000))))
        val res = otpCheck.invoke(getReq(BigDecimal.valueOf(700)))
        assertNotNull(res)
        assertFalse(res.otpRequired)
    }

    private fun getReq(amount : BigDecimal = BigDecimal.valueOf(1000)) =
            OtpCheckRequestDTO(CountryCodes.ghana, 1, AccountType.WALLET, "LETSGO",
                    AccountType.BANK_ACCOUNT, "CITIBANK", amount)

}