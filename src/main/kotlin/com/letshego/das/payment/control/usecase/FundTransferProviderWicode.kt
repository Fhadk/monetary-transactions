package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.KongClient
import com.letshego.das.payment.common.EWALLET
import com.letshego.das.payment.common.PAYMENT_SERVICE
import com.letshego.das.payment.common.WITHDRAWAL_SERVICE
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import com.letshego.das.payment.entity.dto.MerchantPaymentData
import com.letshego.das.payment.entity.dto.WicodePaymentRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FundTransferProviderWicode(private val merchantTransactionClient: MerchantTransactionClient,
                                 private val kongClient: KongClient,
                                 private val eWalletTransactionClient: EWalletTransactionClient,
                                 transactionsRepository: TransactionsRepository,
                                 private val paymentTransactionMapper: PaymentTransactionMapper,
                                 private val paymentStatus: PublishPaymentStatus,
                                 @Value("\${wicode.account-number}")
                                 private val accountNumber: String,
                                 private val namibiaEFTClient: NamibiaEFTClient
)
    : FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    paymentTransactionMapper,
    merchantTransactionClient,
    namibiaEFTClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {

        var status = PaymentStatus.SUCCESS.name
        var transactionId: String? = null
        var responseCode: String? = null
        var message: String?

        val wicodeRequest = createWicodePaymentRequest(request)
        try{
            log.debug("Calling Payment service to initiate wicode payment")
            val response = merchantTransactionClient.processWicodeTransaction(wicodeRequest, request.country?.name.orEmpty())
            log.debug("Response received from payment service: $response")

            if(response.code == 200){
                log.debug("Payment for transaction ref: $transactionRef is successfully initiated.")
            } else{
                log.error("Failure received from payment service for transactionRef: $transactionRef")
                status = PaymentStatus.FAILURE.name
            }

            transactionId = response.data.esbReference
            responseCode = response.code.toString()
            message = "${response.message}: ${response.status}"

            log.info("Message for $transactionRef payment is: $message")
        } catch (e: Exception){
            log.error("Payment for transactionRef: $transactionRef, FAILED", e)
            status = PaymentStatus.FAILURE.name
            message = e.message
        }
        paymentStatus(request.customerId, transactionRef, status, message)
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode.orEmpty(), message)
    }

    private fun createWicodePaymentRequest(dto: PaymentDTO): WicodePaymentRequestDTO{
        if(dto.wicodeTransactionType.equals(PAYMENT_SERVICE, true))
            dto.wicodeTransactionType = WITHDRAWAL_SERVICE

        val cbsData = MerchantPaymentData(accountNumber, EWALLET)
        return WicodePaymentRequestDTO(
            dto.wicodeTransactionType.orEmpty().toLowerCase(),
            dto.toAccountRef.orEmpty(),
            dto.fromAccountRef.orEmpty(),
            true,
            cbsData)
    }
}