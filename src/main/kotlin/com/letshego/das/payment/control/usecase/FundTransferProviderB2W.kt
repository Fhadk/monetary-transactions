package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.PaymentStatusMessageDTO
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.common.EMONEY
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

@Component
class FundTransferProviderB2W(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val paymentClient: UpstreamTransactionClient,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val countryProperties: CountryProperties,
    private val paymentStatus: PublishPaymentStatus,
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
        val b2WankAPIRequest = toW2BRequestAPI(request, transactionRef)
        var status = PaymentStatus.SUCCESS.name
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?

        try {
            log.debug("Calling Kong API Gateway to initiate bank to wallet transaction")
            val response = paymentClient.initiateB2WInternalTransfer(request.country?.name.orEmpty(), b2WankAPIRequest)
            log.debug("Response received from payment service: $response")
            when (response.responseCode) {
                "1" -> {
                    log.debug("Payment for transaction ref: $transactionRef is successfully initiated. KongID: ${response.data.transactionId}")
                }
                else -> {
                    log.error("Failure received from payment service for transactionRef: $transactionRef")
                    status = PaymentStatus.FAILURE.name
                }
            }
            transactionId = response.data.transactionId
            responseCode = response.responseCode
            message = response.responseMessage
            log.info("Message for $transactionRef payment is: $message, status is: $status")
        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }

        val updateStatusDTO = PaymentStatusMessageDTO(request.customerId, request.transactionRef, status, message)
        eWalletTransactionClient.updateTransactionStatus(updateStatusDTO)

        paymentStatus(request.customerId, transactionRef, status, message)
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode, message)

    }

    private fun toW2BRequestAPI(request: PaymentDTO, transactionRef: String): BankToWalletInternalAPIRequest {

        val toUser = MobifinUser(request.toAccountRef.orEmpty())
        val payment = MobifinPayment(request.amount.toPlainString(), "0",
                countryProperties.config[request.country]?.currencyCode.toString(), EMONEY)

        val serviceId = "LETSHEGO_BANK_TO_EWALLET_${request.country?.name.orEmpty().toUpperCase()}"

        return BankToWalletInternalAPIRequest(
                BankToWalletData(
                        toUser, request.fromAccountRef.orEmpty(), transactionRef, payment, serviceId, serviceId,
                        request.message.orEmpty()
                )
        )
    }

}