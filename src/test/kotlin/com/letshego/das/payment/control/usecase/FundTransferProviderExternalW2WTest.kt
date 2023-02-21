package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.client.UpstreamTransactionClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
class FundTransferProviderExternalW2WTest {

    @Mock
    private lateinit var transactionMapper: PaymentTransactionMapper

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var countryProperties: CountryProperties

    @Mock
    private lateinit var kongClient: com.letshego.das.client.wallet.KongClient

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var paymentClient: UpstreamTransactionClient

    @Mock
    private lateinit var publishPaymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @Mock
    private lateinit var eftClient: NamibiaEFTClient

    @InjectMocks
    private lateinit var fundTransferProviderExternalW2W: FundTransferProviderExternalW2W

    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(fundTransferProviderExternalW2W, "paymentStatus", publishPaymentStatus)
    }

    @Test
    fun pay() {
        val req = TestUtil.paymentRequestDto()
        val mobifinRes = TestUtil.getMobifinResponse()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.doReturn(mobifinRes).`when`(paymentClient).initiateExternalWalletToWalletTransfer(any(), any())
        val result = fundTransferProviderExternalW2W.pay(req,"WK1234567890")
        assertNotNull(result)
    }

    @Test
    fun `pay for failure`() {
        val req = TestUtil.paymentRequestDto()
        val mobifinRes = TestUtil.getMobifinResponseForFailure()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        Mockito.doReturn(mobifinRes).`when`(paymentClient).initiateExternalWalletToWalletTransfer(any(), any())
        val result = fundTransferProviderExternalW2W.pay(req,"WK1234567890")
        assertNotNull(result)
    }

    @Test
    fun `pay for Exception`() {
        val req = TestUtil.paymentRequestDto()
        val mobifinRes = TestUtil.getMobifinResponseForFailure()
        val map = HashMap<CountryCodes, CountryProperties.CountryConfig>()
        val config = CountryProperties.CountryConfig()
        config.currencyCode = "NAD"
        map[CountryCodes.namibia] = config
        val spyCountryConfigMap = Mockito.spy(map)
        Mockito.doReturn(spyCountryConfigMap).`when`(countryProperties).config
        Mockito.doReturn(config).`when`(spyCountryConfigMap)[any()]
        //Mockito.doReturn(mobifinRes).`when`(paymentClient).initiateExternalWalletToWalletTransfer(any(), any())
        val result = fundTransferProviderExternalW2W.pay(req,"WK1234567890")
        assertNotNull(result)
    }
}