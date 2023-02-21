package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.KongClient
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class FundTransferProviderBuddyTest {

    @Mock
    private lateinit var kongClient: KongClient

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var paymentTransactionMapper: PaymentTransactionMapper

    @Mock
    private lateinit var paymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @Mock
    private lateinit var countryProperties: CountryProperties

    private val accountNumber: String = "8765677756"

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    private lateinit var fundTransferProviderBuddy: FundTransferProviderBuddy

    @BeforeEach
    fun setUp() {
        fundTransferProviderBuddy = FundTransferProviderBuddy(
            kongClient,
            eWalletTransactionClient,
            transactionsRepository,
            paymentTransactionMapper,
            paymentStatus,
            merchantTransactionClient,
            countryProperties,
            accountNumber,
            eftClient
        )
    }

    @Test
    fun `should pay for success`() {
        val req = TestUtil.getPaymentDto()
        val res1 = TestUtil.getBuddyPaymentResponseDTO()
        Mockito.doReturn(res1).`when`(merchantTransactionClient).processBuddyTransaction(any(), any(), any(), any())
        val result = fundTransferProviderBuddy.pay(req,"txn_7863674568")
        assertEquals("SUCCESS",result.status)
    }

    @Test
    fun `should pay for failure`() {
        val req = TestUtil.getPaymentDto()
        val res1 = TestUtil.getBuddyPaymentResponseDTOForFailure()
        Mockito.doReturn(res1).`when`(merchantTransactionClient).processBuddyTransaction(any(), any(), any(), any())
        val result = fundTransferProviderBuddy.pay(req,"txn_7863674568")
        assertEquals("FAILURE",result.status)
    }

    @Test
    fun `should pay for exeption`() {
        val req = TestUtil.getPaymentDto()
        val res1 = TestUtil.getBuddyPaymentResponseDTOForFailure()
        //Mockito.doReturn(res1).`when`(merchantTransactionClient).processBuddyTransaction(any(), any(), any(), any())
        Mockito.`when`(merchantTransactionClient.processBuddyTransaction(any(), any(), any(), any())).thenThrow(NullPointerException::class.java)
        val result = fundTransferProviderBuddy.pay(req,"txn_7863674568")
        assertEquals("FAILURE",result.status)
    }

}