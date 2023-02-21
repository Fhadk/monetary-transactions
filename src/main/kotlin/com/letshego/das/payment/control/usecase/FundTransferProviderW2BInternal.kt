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
import com.letshego.das.payment.entity.dto.WalletToBankAPIRequest
import com.letshego.das.payment.entity.dto.WalletToBankData
import com.letshego.das.payment.entity.dto.MobifinPayment
import com.letshego.das.payment.entity.dto.MobifinUser
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FundTransferProviderW2BInternal(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val paymentClient: UpstreamTransactionClient,
    private val countryProperties: CountryProperties,
    private val paymentStatus: PublishPaymentStatus,
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

        val w2BankAPIRequest = toW2BRequestAPI(request, transactionRef)

        //TODO Check with P.O. & Panamax if we need to split into 2 cases: internal and external
        var status = PaymentStatus.SUCCESS.name
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?

        try {
            log.debug("Calling Kong API Gateway to initiate wallet to bank transaction")
            val response = paymentClient.initiateWalletToBankTransfer(request.country?.name.orEmpty(), w2BankAPIRequest)
            log.debug("Response received from payment service: $response")
            when (response.responseCode) {
                "1" -> log.debug("Payment for transaction ref: $transactionRef is successfully initiated. KongID: ${response.data.transactionId}")
                else -> {
                    log.error("Failure received from payment service for transactionRef: $transactionRef")
                    status = PaymentStatus.FAILURE.name
                }
            }
            transactionId = response.data.transactionId
            responseCode = response.responseCode.toString()
            message = response.responseMessage
            log.info("Message for $transactionRef payment is: $message, status is: $status")
        } catch (ex: Exception) {
            //if token is expired in kong, re-do transaction
            if(ex is FeignException &&  ex.status() == 401)
                pay(request, transactionRef)

            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }

        val updateStatusDTO = PaymentStatusMessageDTO(request.customerId, request.transactionRef, status, message)
        eWalletTransactionClient.updateTransactionStatus(updateStatusDTO)

        paymentStatus(request.customerId, transactionRef, status, message)
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode, message)

    }

    private fun toW2BRequestAPI(request: PaymentDTO, id: String): WalletToBankAPIRequest {

        val fromUser = MobifinUser(request.fromAccountRef!!)
        val payment = MobifinPayment(request.amount.toPlainString(), "0",
                countryProperties.config[request.country]?.currencyCode.toString(), EMONEY)

        val serviceId = "EWALLET_TO_LETSHEGO_BANK_${request.country?.name.orEmpty().toUpperCase()}"

        return WalletToBankAPIRequest(
                WalletToBankData(
                        fromUser, request.toAccountRef.orEmpty(), id, payment, serviceId, serviceId,
                        request.transactionRef.orEmpty()
                )
        )
    }

}