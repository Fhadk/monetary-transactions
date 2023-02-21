package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.PaymentStatusMessageDTO
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.ms.config.NotificationProperties
import com.letshego.das.ms.web.wallet.TransactionStatus
import com.letshego.das.payment.common.EMONEY
import com.letshego.das.payment.common.EWALLET_TO_EWALLET_P2P
import com.letshego.das.payment.config.PaymentProperties
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.client.UpstreamTransactionClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.text.MessageFormat

@Component
class FundTransferProviderW2WInternal(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val paymentClient: UpstreamTransactionClient,
    private val paymentProperties: PaymentProperties,
    private val paymentStatus: PublishPaymentStatus,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val registrationClient: RegistrationClient,
    private val notificationProperties: NotificationProperties,
    private val countryProperties: CountryProperties,
    private val merchantClient: MerchantTransactionClient,
    private val namibiaEFTClient: NamibiaEFTClient
) : FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    transactionMapper,
    merchantClient,
    namibiaEFTClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status = PaymentStatus.SUCCESS.name
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?
        val response: WalletTransferInternalResponse
        try {
            log.debug("Calling Payment service to initiate wallet payment for ${request.customerId}")
            val requestBody = getRequest(request, transactionRef)

            response = if(request.country?.name.orEmpty() == CountryCodes.botswana.name)
                merchantClient.processInternalWalletTransaction(request = requestBody)
            else
                paymentClient.initiateW2WInternalTransfer(request.country?.name.orEmpty(), requestBody)

            log.debug("Response received from payment service: $response")
            if (response.responseCode == 1)
                log.debug("Payment for transaction ref: $transactionRef is successfully initiated.")
            else {
                log.error("Failure received from payment service for transactionRef: $transactionRef")
                status = PaymentStatus.FAILURE.name
            }
            transactionId = response.data?.transactionId
            responseCode = response.responseCode.toString()
            message = response.responseMessage

            log.info("Message for $transactionRef payment is: $message, status is: $status")
        } catch (ex: RuntimeException) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        if(status == com.letshego.das.ms.web.wallet.PaymentStatus.SUCCESS.name ||
            status == TransactionStatus.COMPLETE.name){

            val transaction = transactionsRepository
                .findByPaymentTransactionIdOrPaymentRefNumber(transactionId.toString(), transactionRef)

            status = PaymentStatus.SUCCESS.name
            val receiver = registrationClient.getCustomerByPhoneNumber(request.toAccountRef.toString())
            val sender = registrationClient.getCustomer(request.customerId)
            val countryCode = countryProperties.config[request.country]?.currencySymbol.toString()

            val template = notificationProperties.template["internal-wallet-in-app"]
            val senderTemplate = template?.get("sender-sms")
            val receiverTemplate = template?.get("receiver-sms")

            var senderMessage = "Transaction Successful"
            if(senderTemplate != null){
                senderMessage = MessageFormat.format(senderTemplate, countryCode, request.amount, receiver.data?.phoneNumber)
            }
            var receiverMessage = "Transaction Successful"
            if(receiverTemplate != null){
                receiverMessage = MessageFormat.format(receiverTemplate, countryCode, request.amount, sender.data?.phoneNumber)
            }

            //send message to receiver
            receiver.data?.customerId?.let { paymentStatus(it, "receiver_"+transaction?.paymentRefNumber!!, status, receiverMessage) }
            //send message to sender
            paymentStatus(request.customerId, "sender_"+transaction?.paymentRefNumber!!, status, senderMessage)
        }
        else
            paymentStatus(request.customerId, transactionRef, status, message)

        val updateStatusDTO = PaymentStatusMessageDTO(request.customerId, request.transactionRef, status, message)
        eWalletTransactionClient.updateTransactionStatus(updateStatusDTO)

        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode, message)
    }

    private fun getRequest(dto: PaymentDTO, transactionRef: String): W2WTransferInternalClientRequest {
        log.debug("Generating W2W payment request for payment service, transactionRef: $transactionRef")
        val props = paymentProperties.config[dto.country]
        val fromUser = MobifinUser(dto.fromAccountRef.orEmpty())
        val toUser = MobifinUser(dto.toAccountRef.orEmpty())
        val country = dto.country?.name?.toUpperCase()
        val payment = MobifinPayment(dto.amount.toPlainString(), BigDecimal.ZERO.toPlainString(),
            props?.currencyCode.orEmpty(), EMONEY)
        val data = W2WInternalReqData(fromUser, toUser, EWALLET_TO_EWALLET_P2P.plus(country),
            EWALLET_TO_EWALLET_P2P.plus(country), transactionRef, dto.message.orEmpty(), listOf(payment))
        val request = W2WTransferInternalClientRequest(data)
        log.debug("Payment service req for transactionRef: $transactionRef is: $request")
        return request
    }

}