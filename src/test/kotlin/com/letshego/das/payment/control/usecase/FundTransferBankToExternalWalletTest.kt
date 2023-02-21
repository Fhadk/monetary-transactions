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
class FundTransferBankToExternalWalletTest {

    private val transactionMapper: PaymentTransactionMapper = Mockito.mock(PaymentTransactionMapper::class.java)

    private val transactionsRepository: TransactionsRepository = Mockito.mock(TransactionsRepository::class.java)

    private val rubyClient: RubyClient = Mockito.mock(RubyClient::class.java)

    private val cbsClient: CBSClient = Mockito.mock(CBSClient::class.java)

    private val kongClient: com.letshego.das.client.wallet.KongClient =
        Mockito.mock(com.letshego.das.client.wallet.KongClient::class.java)

    private val eWalletTransactionClient: EWalletTransactionClient = Mockito.mock(EWalletTransactionClient::class.java)

    private val countryProperties: CountryProperties = Mockito.mock(CountryProperties::class.java)

    private val merchantTransactionClient: MerchantTransactionClient = Mockito.mock(MerchantTransactionClient::class.java)

    private val namibiaEFTClient: NamibiaEFTClient = Mockito.mock(NamibiaEFTClient::class.java)

    private val fundTransferBankToExternalWallet: FundTransferBankToWalletExternal = Mockito.spy(
        FundTransferBankToWalletExternal(
            transactionMapper,
            transactionsRepository,
            rubyClient,
            cbsClient,
            kongClient,
            eWalletTransactionClient,
            countryProperties,
            merchantTransactionClient,
            namibiaEFTClient,
            "url"
        )
    )

    @Test
    fun `should pay for bank to external wallet for namibia success`() {
        val req = TestUtil.paymentRequestDto()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToExternalWalletResponseNm()
        Mockito.doReturn(returnValue).`when`(cbsClient).walletTransferInitiate(any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToExternalWallet.pay(req,"WK123987645")
        assertNotNull(result)
    }

    @Test
    fun `should pay for bank to external wallet for namibia failure`() {
        val req = TestUtil.paymentRequestDto()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToExternalWalletResponseNm()
        Mockito.doReturn(returnValue).`when`(cbsClient).walletTransferInitiate(any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToExternalWallet.pay(req,"WK123987645")
        assertNotNull(result)
    }

    @Test
    fun `should pay for bank to external wallet for ghana success`() {
        val req = TestUtil.paymentRequestDtoForGhana()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "GHA"
        map[CountryCodes.ghana] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToExternalWalletResponseGh()
        Mockito.doReturn(returnValue).`when`(rubyClient).bankToExternalWalletTransferInitiate(any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToExternalWallet.pay(req,"WK123987645")
        assertNotNull(result)
    }

    @Test
    fun `should pay for bank to external wallet for ghana failure`() {
        val req = TestUtil.paymentRequestDtoForGhana()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "GHA"
        map[CountryCodes.ghana] = config
        val spyCountryConfigMap = Mockito.spy(map)
        val returnValue = TestUtil.getBankToExternalWalletResponseGh()
        Mockito.doReturn(returnValue).`when`(rubyClient).bankToExternalWalletTransferInitiate(any())
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        val transaction = Transactions(
            req.customerId, req.fromAccountType, req.fromAccountName, req.fromAccountRef,
            req.fromAccountCode, req.toAccountType, req.toAccountName, req.toAccountRef,
            req.toAccountCode, req.amount, req.transactionRef
        )
        Mockito.`when`(transactionMapper.toEntity(req)).thenReturn(transaction)
        val result = fundTransferBankToExternalWallet.pay(req,"WK123987645")
        assertNotNull(result)
    }
}