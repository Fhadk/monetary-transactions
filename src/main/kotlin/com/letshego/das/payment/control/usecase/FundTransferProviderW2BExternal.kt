package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.common.WALLET_TO_BANK_EXTERNAL
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
class FundTransferProviderW2BExternal(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val paymentClient: UpstreamTransactionClient,
    private val countryProperties: CountryProperties,
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

        val w2BankAPIRequest = toWalletToBankAPI(request, transactionRef)

        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?

        try {
            log.debug("Calling Kong API Gateway to initiate wallet to bank external transaction")
            val response = paymentClient.initiateWalletToBankExternalTransfer(request.country?.name.orEmpty(), w2BankAPIRequest)
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
        publishPaymentStatus(request.customerId, transactionRef, status, message.orEmpty())
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode, message)
    }

    private fun toWalletToBankAPI(request: PaymentDTO, transactionRef: String): WalletToBankExternalAPIRequest{

        val data: BaseWalletToBankExternalData
        val serviceId = "${WALLET_TO_BANK_EXTERNAL}${request.country?.name?.toUpperCase()}"

        //Use factory pattern here to dynamically create obj of type BaseWalletToBankExternalData depending on country
        when(request.country){
            CountryCodes.ghana -> {
                data = GhanaWalletToBankExternalAPIRequest(request.fromAccountRef.orEmpty(),
                    serviceId,  serviceId, transactionRef,
                    request.amount, countryProperties.config[request.country]?.currencyCode.toString(),
                    request.message ?: WALLET_TO_BANK_EXTERNAL, request.toAccountCode.orEmpty(),
                    request.toAccountName.orEmpty(), request.toAccountRef.orEmpty())
            }

            CountryCodes.namibia -> {
                data = NamibiaWalletToBankExternalAPIRequest(request.fromAccountRef.orEmpty(),
                    serviceId, serviceId, transactionRef, request.amount,
                    countryProperties.config[request.country]?.currencyCode.toString(),
                    request.message ?: WALLET_TO_BANK_EXTERNAL, request.toAccountName.orEmpty(),
                    request.message ?: WALLET_TO_BANK_EXTERNAL, request.toAccountCode.orEmpty(),
                    request.toAccountRef.orEmpty())
            }

            else ->
                throw ApplicationException(ErrorCodes.COUNTRY_NOT_SUPPORTED)
        }

        return WalletToBankExternalAPIRequest(data)
    }
}