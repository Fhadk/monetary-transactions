package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.client.UpstreamTransactionClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.mapper.PaymentTransactionMapperImpl
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class FundTransferProviderB2WTest {

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var paymentClient: UpstreamTransactionClient

    @Mock
    private lateinit var kongClient: com.letshego.das.client.wallet.KongClient

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var countryProperties: CountryProperties

    @Mock
    private lateinit var paymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    private lateinit var fundTransferProviderB2W: FundTransferProviderB2W

    @BeforeEach
    fun setUp() {
        fundTransferProviderB2W = FundTransferProviderB2W(
            PaymentTransactionMapperImpl(), transactionsRepository,
            paymentClient,kongClient , eWalletTransactionClient, countryProperties,
            paymentStatus,merchantTransactionClient,eftClient)

        //ReflectionTestUtils.setField(paymentW2WInternal, "errorProperties", paymentErrorProperties)
    }

    @Test
    fun `should pay for success`() {
        val req = TestUtil.paymentRequestDto()
        val returnValue = TestUtil.getMobifinResponse()
        Mockito.doReturn(returnValue).`when`(paymentClient).initiateB2WInternalTransfer(any(), any())
        Mockito.doNothing().`when`(eWalletTransactionClient).updateTransactionStatus(any())
        val result = fundTransferProviderB2W.pay(req,"txn_9087654321")
        assertEquals("SUCCESS",result.status)
    }

    @Test
    fun `should pay for failure`() {
        val req = TestUtil.paymentRequestDto()
        val returnValue = TestUtil.getMobifinResponseForFailure()
        Mockito.doReturn(returnValue).`when`(paymentClient).initiateB2WInternalTransfer(any(), any())
        Mockito.doNothing().`when`(eWalletTransactionClient).updateTransactionStatus(any())
        val result = fundTransferProviderB2W.pay(req,"txn_9087654321")
        assertEquals("FAILURE",result.status)
    }
}