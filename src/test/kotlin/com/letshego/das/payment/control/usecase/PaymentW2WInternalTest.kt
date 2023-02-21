package com.letshego.das.payment.control.usecase

import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.ms.config.NotificationProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.config.PaymentErrorProperties
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.client.UpstreamTransactionClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapperImpl
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.dto.*
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class PaymentW2WInternalTest {

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var paymentClient: UpstreamTransactionClient

    @Mock
    private lateinit var paymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var paymentErrorProperties: PaymentErrorProperties

    private lateinit var paymentW2WInternal: FundTransferProviderW2WInternal

    @Mock
    private lateinit var kongClient: com.letshego.das.client.wallet.KongClient

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var registrationClient: RegistrationClient

    @Mock
    private lateinit var notificationProperties: NotificationProperties

    @Mock
    private lateinit var countryProperties: CountryProperties

    @Mock
    private lateinit var merchantClient: MerchantTransactionClient

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    @BeforeEach
    fun init() {
        paymentW2WInternal = FundTransferProviderW2WInternal(PaymentTransactionMapperImpl(), transactionsRepository,
                paymentClient, TestUtil.getPaymentProperties(), paymentStatus, kongClient, eWalletTransactionClient,
                registrationClient, notificationProperties, countryProperties, merchantClient,eftClient)

        ReflectionTestUtils.setField(paymentW2WInternal, "errorProperties", paymentErrorProperties)

    }


    @Test
    fun `should perform wallet to wallet internal transaction`() {
        Mockito.`when`(paymentClient.initiateW2WInternalTransfer(any(), any()))
                .thenReturn(walletTransferInternalResponse())
        Mockito.`when`(transactionsRepository.save(any())).thenReturn(TestUtil.getTransaction())
        Mockito.`when`(transactionsRepository.findByPaymentTransactionIdOrPaymentRefNumber(any(), any()))
            .thenReturn(TestUtil.getTransaction())
        Mockito.`when`(registrationClient.getCustomerByPhoneNumber(any()))
            .thenReturn(TestUtil.getCustomer())
        val res = paymentW2WInternal.transfer(TestUtil.getPaymentRequest())
        assertNotNull(res)
        assertEquals("SUCCESS", res.status)
    }

    @Test
    fun `should update status to failed if error from transaction API`() {
        Mockito.`when`(paymentClient.initiateW2WInternalTransfer(any(), any()))
                .thenReturn(walletTransferInternalResponse(0))
        Mockito.`when`(transactionsRepository.save(any())).thenReturn(TestUtil.getTransaction())
        val res = paymentW2WInternal.transfer(TestUtil.getPaymentRequest())
        assertNotNull(res)
        assertEquals("FAILURE", res.status)
    }

    @Test
    fun `should return failure if error from transaction API`() {
        Mockito.`when`(transactionsRepository.save(any())).thenReturn(TestUtil.getTransaction())
        Mockito.`when`(paymentClient.initiateW2WInternalTransfer(any(), any()))
                .thenReturn(WalletTransferInternalResponse(W2WInternalResponse("tr123"), "", 2))
        val res = paymentW2WInternal.transfer(TestUtil.getPaymentRequest())
        assertNotNull(res)
        assertEquals("FAILURE", res.status)
    }


    private fun walletTransferInternalResponse(responseCode: Int = 1): WalletTransferInternalResponse {
        val data = W2WInternalResponse("TR123456789")
        return WalletTransferInternalResponse(data, "Transaction completed", responseCode)
    }

}