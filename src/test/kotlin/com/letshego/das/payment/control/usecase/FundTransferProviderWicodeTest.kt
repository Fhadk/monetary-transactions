package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.KongClient

import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
class FundTransferProviderWicodeTest {

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

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

    private val accountNumber: String = "Wk12345678"

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    private lateinit var fundTransferProviderWicode: FundTransferProviderWicode

    @BeforeEach
    fun setUp() {
        fundTransferProviderWicode = FundTransferProviderWicode(
            merchantTransactionClient,
            kongClient,
            eWalletTransactionClient,
            transactionsRepository,
            paymentTransactionMapper,
            paymentStatus,
            accountNumber,
            eftClient
        )
    }

    @Test
    fun pay() {
        val req = TestUtil.paymentRequestDtoForWicode()
        val successResponse = TestUtil.getWicodePaymentResponseDTO()
        Mockito.doReturn(successResponse).`when`(merchantTransactionClient).processWicodeTransaction(any(), any(), any(), any())
        val result = fundTransferProviderWicode.pay(req,"WK1234567890")
        assertNotNull(result)
    }

    @Test
    fun `pay for failure`() {
        val req = TestUtil.paymentRequestDtoForWicode()
        val successResponse = TestUtil.getWicodePaymentResponseDTOForFailure()
        Mockito.doReturn(successResponse).`when`(merchantTransactionClient).processWicodeTransaction(any(), any(), any(), any())
        val result = fundTransferProviderWicode.pay(req,"WK1234567890")
        assertNotNull(result)
    }

    @Test
    fun `pay for Exception`() {
        val req = TestUtil.paymentRequestDtoForWicode()
        val successResponse = TestUtil.getWicodePaymentResponseDTO()
        //Mockito.doReturn(successResponse).`when`(merchantTransactionClient).processWicodeTransaction(any(), any())
        val result = fundTransferProviderWicode.pay(req,"WK1234567890")
        assertNotNull(result)
    }
}