package com.letshego.das.payment.control.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.letshego.das.client.wallet.KongClient
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.CBSClient
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class FundTransferProviderBankInternalTest {

    @Mock
    private lateinit var cbsClient: CBSClient

    @Mock
    private lateinit var kongClient: KongClient

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @Mock
    private lateinit var transactionMapper: PaymentTransactionMapper

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    @InjectMocks
    private lateinit var fundTransferProviderBankInternal: FundTransferProviderBankInternal

    @Test
    fun `should pay for success`() {
        val req = TestUtil.getPaymentDto();
        val res = TestUtil.getCBSGatewayResponse()
        Mockito.doReturn(res).`when`(cbsClient).cbsCall(any(), any(), any(), any())
        val result = fundTransferProviderBankInternal.pay(req,"txn12344333")
        assertEquals("SUCCESS",result.status)
    }

    @Test
    fun `should pay for failure`() {
        val req = TestUtil.getPaymentDto();
        val res = TestUtil.getCBSGatewayResponseForFailure()
        Mockito.doReturn(res).`when`(cbsClient).cbsCall(any(), any(), any(), any())
        val result = fundTransferProviderBankInternal.pay(req,"txn12344333")
        assertEquals("FAILURE",result.status)
    }
}