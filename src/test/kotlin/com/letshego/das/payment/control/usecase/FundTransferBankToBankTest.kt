package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.*
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.Transactions
import com.nhaarman.mockitokotlin2.any
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class FundTransferBankToBankTest {

    private val transactionMapper: PaymentTransactionMapper = Mockito.mock(PaymentTransactionMapper::class.java)

    private val transactionsRepository: TransactionsRepository = Mockito.mock(TransactionsRepository::class.java)

    private val rubyClient: RubyClient = Mockito.mock(RubyClient::class.java)

    private val cbsClient: CBSClient = Mockito.mock(CBSClient::class.java)

    private val kongClient: com.letshego.das.client.wallet.KongClient = Mockito.mock(com.letshego.das.client.wallet.KongClient::class.java)

    private val eWalletTransactionClient: EWalletTransactionClient = Mockito.mock(EWalletTransactionClient::class.java)

    private val countryProperties: CountryProperties = Mockito.mock(CountryProperties::class.java)

    private val merchantTransactionClient: MerchantTransactionClient = Mockito.mock(MerchantTransactionClient::class.java)

    private val namibiaEFTClient: NamibiaEFTClient = Mockito.mock(NamibiaEFTClient::class.java)

    private val updateTransactionStatus: UpdateTransactionStatus = Mockito.mock(UpdateTransactionStatus::class.java)

    private val fundTransferBankToBank: FundTransferBankToBank = Mockito.spy(
        FundTransferBankToBank(
            transactionMapper,
            transactionsRepository,
            rubyClient,
            cbsClient,
            kongClient,
            eWalletTransactionClient,
            countryProperties,
            merchantTransactionClient,
            namibiaEFTClient,
            updateTransactionStatus
        )
    )


    @Test
    fun `should pay for bank to bank for namibia success`() {
        val req = TestUtil.paymentRequestDto()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToBankApiResponseForNamibia()
        Mockito.doReturn(returnValue).`when`(cbsClient).bankTransferInitiate(any(), any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToBank.pay(req,"WK123987645")
        assertNotNull(result)
    }

    @Test
    fun `should pay for bank to bank for namibia failure`() {
        val req = TestUtil.paymentRequestDto()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToBankApiResponseForNamibiaForFailure()
        Mockito.doReturn(returnValue).`when`(cbsClient).bankTransferInitiate(any(), any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToBank.pay(req,"WK123987645")
        assertNotNull(result)
    }

    @Test
    fun `should pay for bank to bank for ghana success`() {
        val req = TestUtil.paymentRequestDtoForGhana()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        config.currencyCode = "GHA"
        map[CountryCodes.ghana] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToBankApiResponse()
        Mockito.doReturn(returnValue).`when`(rubyClient).externalBankTransferInitiate(any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToBank.pay(req,"WK123987645")
        assertNotNull(result)
    }

    @Test
    fun `should pay for bank to bank for ghana failure`() {
        val req = TestUtil.paymentRequestDtoForGhana()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        config.currencyCode = "GHA"
        map[CountryCodes.ghana] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToBankApiResponseForFailure()
        Mockito.doReturn(returnValue).`when`(rubyClient).externalBankTransferInitiate(any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToBank.pay(req,"WK123987645")
        assertNotNull(result)
    }

}