package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.common.EMONEY
import com.letshego.das.payment.common.EXTERNAL_WALLET_TO_EWALLET
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
class FundTransferProviderExternalW2W(transactionMapper: PaymentTransactionMapper,
                                      transactionsRepository: TransactionsRepository,
                                      private val countryProperties: CountryProperties,
                                      private val kongClient: com.letshego.das.client.wallet.KongClient,
                                      private val eWalletTransactionClient: EWalletTransactionClient,
                                      private val paymentClient: UpstreamTransactionClient,
                                      private val merchantTransactionClient: MerchantTransactionClient,
                                      private val namibiaEFTClient: NamibiaEFTClient
) : FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    transactionMapper,
    merchantTransactionClient,
    namibiaEFTClient
){

    private val log = LoggerFactory.getLogger(javaClass)

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {

        val externalW2WRequest = toWalletToWalletAPI(request, transactionRef)

        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var message: String?
        var responseCode = "500"

        try{
            log.debug("Calling Kong API Gateway to initiate external wallet to wallet transaction")
            val response = paymentClient.initiateExternalWalletToWalletTransfer(request.country?.name.orEmpty(),
                externalW2WRequest)
            log.debug("Response received from payment service: $response")
            when (response.responseCode) {
                "1" -> log.debug("Payment for transaction ref: $transactionRef is successfully initiated. KongID:" +
                        " ${response.data.transactionId}")
                else -> {
                    log.error("Failure received from payment service for transactionRef: $transactionRef")
                    status = PaymentStatus.FAILURE.name
                }
            }
            transactionId = response.data.transactionId
            responseCode = response.responseCode.toString()
            message = response.responseMessage
            log.info("Message for $transactionRef payment is: $message, status is: $status")

        } catch (ex: Exception){
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

    private fun toWalletToWalletAPI(request: PaymentDTO, transactionRef: String): ExternalW2WTransferClientRequest {

        val serviceId = "$EXTERNAL_WALLET_TO_EWALLET${request.country?.name?.toUpperCase()}"

        val payment = MobifinPayment(request.amount.toPlainString(), "0",
            countryProperties.config[request.country]?.currencyCode.toString(), EMONEY)

        val data = ExternalW2WRequestData(
            MobifinUser(request.toAccountRef.orEmpty()), serviceId, serviceId, transactionRef,
            request.toAccountRef.orEmpty(), request.message ?: EXTERNAL_WALLET_TO_EWALLET,
            request.toAccountCode ?: request.fromAccountName.orEmpty(), listOf(payment)
        )

        return ExternalW2WTransferClientRequest(data)
    }
}