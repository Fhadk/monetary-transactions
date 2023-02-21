package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.config.PaymentErrorProperties
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.client.UpstreamTransactionClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapperImpl
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
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
class PaymentW2WExternalTest {

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var paymentErrorProperties: PaymentErrorProperties

    @Mock
    private lateinit var paymentClient: UpstreamTransactionClient

    @Mock
    private lateinit var paymentW2WInternal: FundTransferProviderW2WExternal

    @Mock
    private lateinit var paymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var kongClient: com.letshego.das.client.wallet.KongClient

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    @BeforeEach
    fun init() {
        paymentW2WInternal = FundTransferProviderW2WExternal(
            PaymentTransactionMapperImpl(),
            transactionsRepository,
            paymentClient,
            TestUtil.getPaymentProperties(),
            kongClient,
            eWalletTransactionClient,
            merchantTransactionClient,
            eftClient
        )

        ReflectionTestUtils.setField(paymentW2WInternal, "paymentStatus", paymentStatus)
        ReflectionTestUtils.setField(paymentW2WInternal, "errorProperties", paymentErrorProperties)

    }


    @Test
    fun `should perform wallet to wallet internal transaction`() {
        Mockito.`when`(paymentClient.initiateW2WExternalTransfer(any(), any()))
                .thenReturn(walletTransferInternalResponse())
        Mockito.`when`(transactionsRepository.save(any())).thenReturn(TestUtil.getTransaction())
        val res = paymentW2WInternal.transfer(TestUtil.getPaymentRequest())
        assertNotNull(res)
        assertEquals(PaymentStatus.IN_PROGRESS.name, res.status)
    }

    @Test
    fun `should update status to failed if error from transaction API`() {
        Mockito.`when`(paymentClient.initiateW2WExternalTransfer(any(), any()))
                .thenReturn(walletTransferInternalResponse(0))
        Mockito.`when`(transactionsRepository.save(any())).thenReturn(TestUtil.getTransaction())
        val res = paymentW2WInternal.transfer(TestUtil.getPaymentRequest())
        assertNotNull(res)
        assertEquals(PaymentStatus.FAILURE.name, res.status)
    }

    @Test
    fun `should return failure if error from transaction API`() {
        Mockito.`when`(paymentClient.initiateW2WExternalTransfer(any(), any()))
                .thenThrow(ApplicationException(ErrorCodes.INTERNAL_ERROR))
        Mockito.`when`(transactionsRepository.save(any())).thenReturn(TestUtil.getTransaction())
        val res = paymentW2WInternal.transfer(TestUtil.getPaymentRequest())
        assertNotNull(res)
        assertEquals(PaymentStatus.FAILURE.name, res.status)
    }


    private fun walletTransferInternalResponse(responseCode: Int = 1): WalletTransferInternalResponse {
        val data = W2WInternalResponse("TR123456789")
        return WalletTransferInternalResponse(data, "Transaction completed", responseCode)
    }

}