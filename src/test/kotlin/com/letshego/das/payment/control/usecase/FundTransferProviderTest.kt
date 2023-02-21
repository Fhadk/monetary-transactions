package com.letshego.das.payment.control.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.letshego.das.client.wallet.KongClient
import com.letshego.das.ms.CountryCodes
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.common.KONG_METHOD_TRANSFER_TO_DEPOSIT
import com.letshego.das.payment.common.KONG_SERVICE_NAME
import com.letshego.das.payment.control.client.CBSClient
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.dto.CBSTransferDepositGatewayRequest
import com.nhaarman.mockitokotlin2.times
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito

import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExtendWith(MockitoExtension::class)
class FundTransferProviderTest {
    lateinit var fundTransferProviderBankInternal: FundTransferProviderBankInternal

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

    @BeforeEach
    fun init() {
        fundTransferProviderBankInternal = FundTransferProviderBankInternal(
            cbsClient,
            kongClient,
            objectMapper,
            transactionMapper,
            transactionsRepository,
            eWalletTransactionClient,
            merchantTransactionClient,
            eftClient
        )

    }

    @Test
    fun `should transfer fund to internal bank account successfully`() {
        Mockito.`when`(
            cbsClient.cbsCall(
                "botswana",
                "cbs",
                "transfer-to-deposit",
                CBSTransferDepositGatewayRequest(
                    fromAccountNumber = null,
                    toAccountNumber = null,
                    amount = BigDecimal.ZERO,
                    transactionAmount = BigDecimal.ZERO,
                    transactionCurrencyCode = null,
                    accountCurrencyCode = null,
                    statementNarration = "Internal Transfer",
                    country = "botswana"
                )
            )
        ).thenReturn(TestUtil.getCBSGatewayResponse())
        val paymentResponseDTO = fundTransferProviderBankInternal.pay(TestUtil.getPaymentDto(), "12313")
        assertEquals("SUCCESS", paymentResponseDTO.status)
        assertEquals("12313", paymentResponseDTO.paymentRef)
        Mockito.verify(cbsClient, times(1)).cbsCall(
            "botswana",
            KONG_SERVICE_NAME,
            KONG_METHOD_TRANSFER_TO_DEPOSIT, CBSTransferDepositGatewayRequest(
                null,
                null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                null,
                null,
                "Internal Transfer",
                CountryCodes.botswana.name
            )
        )

    }

    @Test
    fun `should not transfer fund to internal bank account successfully`() {
        Mockito.`when`(
            cbsClient.cbsCall(
                "botswana",
                "cbs",
                "transfer-to-deposit",
                CBSTransferDepositGatewayRequest(
                    fromAccountNumber = null,
                    toAccountNumber = null,
                    amount = BigDecimal.ZERO,
                    transactionAmount = BigDecimal.ZERO,
                    transactionCurrencyCode = null,
                    "botswana",
                    statementNarration = "Internal Transfer",
                    country = "botswana"
                )
            )
        ).thenReturn(TestUtil.getCBSGatewayResponse())
        val paymentResponseDTO = fundTransferProviderBankInternal.pay(TestUtil.getPaymentDto(), "1231")
        assertNotEquals("Success", paymentResponseDTO.status)
        assertNotEquals("1233", paymentResponseDTO.paymentRef)
        assertEquals("500", paymentResponseDTO.responseCode)
        Mockito.verify(cbsClient, times(1)).cbsCall(
            "botswana",
            KONG_SERVICE_NAME,
            KONG_METHOD_TRANSFER_TO_DEPOSIT, CBSTransferDepositGatewayRequest(
                null,
                null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                null,
                null,
                "Internal Transfer",
                CountryCodes.botswana.name
            )
        )

    }
}