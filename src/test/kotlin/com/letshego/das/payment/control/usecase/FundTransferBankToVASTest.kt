package com.letshego.das.payment.control.usecase

import com.letshego.das.client.vas.VasClient
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.config.UtilityProperties
import com.letshego.das.payment.control.client.CBSClient
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.Transactions
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FundTransferBankToVASTest {

    private val transactionMapper: PaymentTransactionMapper = Mockito.mock(PaymentTransactionMapper::class.java)

    private val transactionsRepository: TransactionsRepository = Mockito.mock(TransactionsRepository::class.java)

    private val hubtleVasBillPayClient: VasClient = Mockito.mock(VasClient::class.java)

    private val cbsClient: CBSClient = Mockito.mock(CBSClient::class.java)

    private val kongClient: com.letshego.das.client.wallet.KongClient =
        Mockito.mock(com.letshego.das.client.wallet.KongClient::class.java)

    private val eWalletTransactionClient: EWalletTransactionClient = Mockito.mock(EWalletTransactionClient::class.java)

    private val countryProperties: CountryProperties = Mockito.mock(CountryProperties::class.java)

    private val utilityProperties: UtilityProperties = Mockito.mock(UtilityProperties::class.java)

    private val merchantTransactionClient: MerchantTransactionClient = Mockito.mock(MerchantTransactionClient::class.java)

    private val namibiaEFTClient: NamibiaEFTClient = Mockito.mock(NamibiaEFTClient::class.java)

    private val fundTransferBankToVAS:FundTransferBankToVAS = Mockito.spy(
        FundTransferBankToVAS(
            transactionMapper,
            transactionsRepository,
            hubtleVasBillPayClient,
            kongClient,
            eWalletTransactionClient,
            countryProperties,
            cbsClient,
            utilityProperties,
            merchantTransactionClient,
            namibiaEFTClient,
            "url"
        )
    )

    @Test
    fun `should pay for success`() {
        val req = TestUtil.paymentRequestDto()
        val returnValue = TestUtil.getHubtleBillPayResponse()
        Mockito.doReturn(returnValue).`when`(hubtleVasBillPayClient).payVASBills(any())
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToVAS.pay(req,"txn_9087654321")
        assertNotNull(result)
    }

    @Test
    fun `should pay for failure`() {
        val req = TestUtil.paymentRequestDto()
        val returnValue = TestUtil.getHubtleBillPayResponseForFailure()
        Mockito.doReturn(returnValue).`when`(hubtleVasBillPayClient).payVASBills(any())
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToVAS.pay(req,"txn_9087654321")
        assertNotNull(result)
    }

    @Test
    fun `should pay for exception`() {
        val req = TestUtil.paymentRequestDto()
        val returnValue = TestUtil.getHubtleBillPayResponseForFailure()
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToVAS.pay(req,"txn_9087654321")
        assertNotNull(result)
    }


}