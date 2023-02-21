package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.KongClient
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapperImpl
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
internal class FundTransferProviderExternalP2MTest {

    @Mock
    private lateinit var kongClient: KongClient

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var merchantClient: MerchantClient

    @Mock
    private lateinit var publishPaymentStatus: PublishPaymentStatus

    private val id: Long = 9976334787

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    private lateinit var fundTransferProviderExternalP2M: FundTransferProviderExternalP2M

    @BeforeEach
    fun setUp() {
        fundTransferProviderExternalP2M = FundTransferProviderExternalP2M(
            PaymentTransactionMapperImpl(),transactionsRepository,
            kongClient,eWalletTransactionClient,merchantClient,id,
        merchantTransactionClient,eftClient)
        ReflectionTestUtils.setField(fundTransferProviderExternalP2M, "paymentStatus", publishPaymentStatus)
    }

    @Test
    fun `should pay for success`() {
        val req = TestUtil.getPaymentDto()
        val res1 = TestUtil.getPayMerchantAPIResponse()
        Mockito.doReturn(res1).`when`(merchantClient).payMerchant(any(), any())
        val result = fundTransferProviderExternalP2M.pay(req,"txn_7863674568")
        kotlin.test.assertEquals("SUCCESS", result.status)
    }

    @Test
    fun `should pay for failure`() {
        val req = TestUtil.getPaymentDto()
        val res1 = TestUtil.getPayMerchantAPIResponseForFailure()
        Mockito.doReturn(res1).`when`(merchantClient).payMerchant(any(), any())
        val result = fundTransferProviderExternalP2M.pay(req,"txn_7863674568")
        kotlin.test.assertEquals("IN_PROGRESS", result.status)
    }

    @Test
    fun `should pay for exeption`() {
        val req = TestUtil.getPaymentDto()
        val res1 = TestUtil.getBuddyPaymentResponseDTOForFailure()
        Mockito.`when`(merchantClient.payMerchant(any(), any())).thenThrow(NullPointerException::class.java)
        val result = fundTransferProviderExternalP2M.pay(req,"txn_7863674568")
        kotlin.test.assertEquals("FAILURE", result.status)
    }
}