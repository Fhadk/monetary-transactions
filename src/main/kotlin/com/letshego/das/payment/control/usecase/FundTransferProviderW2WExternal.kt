package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.CountryCodes
import com.letshego.das.payment.config.PaymentProperties
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.client.UpstreamTransactionClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.*
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FundTransferProviderW2WExternal(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val paymentClient: UpstreamTransactionClient,
    private val paymentProperties: PaymentProperties,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val merchantTransactionClient: MerchantTransactionClient,
    private val namibiaEFTClient: NamibiaEFTClient
) : FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    transactionMapper,
    merchantTransactionClient,
    namibiaEFTClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?
        try {
            log.debug("Calling Payment service to initiate wallet payment")
            val response = paymentClient
                .initiateW2WExternalTransfer(request.country?.name.orEmpty(), getRequest(request, transactionRef))
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
            //if token is expired in kong, re-do transaction
            if(ex is FeignException &&  ex.status() == 401)
                pay(request, transactionRef)

            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        publishPaymentStatus(request.customerId, transactionRef, status, message.orEmpty())
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode, message)
    }

    private fun getRequest(dto: PaymentDTO, transactionRef: String): W2WTransferExternalClientRequest {
        log.debug("Generating W2W payment request for payment service, transactionRef: $transactionRef")
        val props = paymentProperties.config[dto.country]
        val fromUser = MobifinUser(dto.fromAccountRef.orEmpty())
        val country = dto.country?.name?.toUpperCase()
        val payment = MobifinPayment(dto.amount.toPlainString(), "0", props?.currencyCode.orEmpty(), "EMONEY")
        val phoneCode = if(dto.country == CountryCodes.namibia) CountryCodes.namibia.phoneCode.replace("+", "") else ""

        val data = W2WExternalReqData(
            fromUser,
            phoneCode.plus(dto.toAccountRef.orEmpty()),
            "EWALLET_TO_EXTERNAL_WALLET_$country",
            "EWALLET_TO_EXTERNAL_WALLET_$country",
            transactionRef,
            dto.message.orEmpty(),
            dto.toAccountCode ?: dto.fromAccountName.orEmpty(),
            listOf(payment)
        )
        val req = W2WTransferExternalClientRequest(data)
        log.debug("Payment service req for transactionRef: $transactionRef is: $req")
        return req
    }

}