package com.letshego.das.payment.control.usecase

import com.amazonaws.services.sns.AmazonSNSClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.letshego.das.client.notification.NotificationClient
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.client.wallet.KongClient
import com.letshego.das.client.wallet.PaymentStatusMessageDTO
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.config.NotificationProperties
import com.letshego.das.ms.web.APIResult
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.times
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class PublishPaymentStatusTest {

    @Mock
    private lateinit var client: AmazonSNSClient

    private val paymentStatusTopic: String = "arn:aws:sns:eu-west-1:116201929255:payment-status"

    @Mock
    private lateinit var notificationClient: NotificationClient

    @Mock
    private lateinit var notificationProperties: NotificationProperties

    @Mock
    private lateinit var kongClient: KongClient

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Mock
    private lateinit var registrationClient: RegistrationClient

    private lateinit var publishPaymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var publishPaymentStatusMock: PublishPaymentStatus

    @Mock
    private lateinit var merchantTransactionClient: MerchantTransactionClient

    @BeforeEach
    fun setUp() {
        publishPaymentStatus = Mockito.spy(
            PublishPaymentStatus(
                client,
                ObjectMapper(),
                paymentStatusTopic,
                notificationClient,
                notificationProperties,
                kongClient,
                eWalletTransactionClient,
                registrationClient,
                merchantTransactionClient,
                "down"
            )
        )
    }

    @Test
    fun `should send push notification or sms when payment is success`() {
        val data = PaymentStatusMessageDTO(768998, "WK768998", "SUCCESS", "", "");

        mockTemplateProperty()

        mockCountryCodeProperty()

        mockSendCreditDebitNotification()

        Mockito.doReturn(APIResult.success("success",null)).`when`(notificationClient).notify(any())

        publishPaymentStatus.invoke(768998, "WK768998", "SUCCESS", "", "")
        Mockito.verify(notificationClient, times(2)).notify(any())
    }

    private fun mockTemplateProperty() {
        val templateMap = Mockito.spy(HashMap<String, Map<String, String>>())
        Mockito.lenient().doReturn(templateMap).`when`(notificationProperties).template
        val iTemplateMap = Mockito.spy(HashMap<String, String>())
        iTemplateMap["push"] = "Hello Test"
        iTemplateMap["namibia"] = "Hello your balance"
        Mockito.lenient().doReturn(iTemplateMap).`when`(templateMap)["internal-wallet-in-app"]
    }

    private fun mockCountryCodeProperty(){
        val countryConfig = NotificationProperties.ConstantConfig();
        countryConfig.currency = "dollar"
        val constant = Mockito.spy(HashMap<CountryCodes, NotificationProperties.ConstantConfig>())
        constant[CountryCodes.namibia] = countryConfig
        constant[CountryCodes.botswana] = countryConfig
        Mockito.lenient().doReturn(constant).`when`(notificationProperties).constant
        Mockito.lenient().doReturn(countryConfig).`when`(constant)[anyOrNull()]
    }

    private fun mockSendCreditDebitNotification(){
        val apiCustomerResponse = TestUtil.getCustomer()
        val apiTransactionDetailDto = TestUtil.getTransactionDetailDTOResponse()
        val apiWalletResponse = TestUtil.sampleWalletBalance()
        Mockito.lenient().doReturn(apiTransactionDetailDto).`when`(eWalletTransactionClient).getWallet(any())
        Mockito.lenient().doReturn(apiCustomerResponse).`when`(registrationClient).getCustomer(any())
        Mockito.lenient().doReturn(apiWalletResponse).`when`(kongClient).getWalletBalance(any(), any())
    }

}