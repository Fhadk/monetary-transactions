package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.EMPTY_STRING
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.config.PaymentProperties
import com.letshego.das.payment.config.UtilityProperties
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
import java.lang.RuntimeException

@Component
class WalletVASPayment(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val paymentClient: UpstreamTransactionClient,
    private val paymentProperties: PaymentProperties,
    private val utilityProperties: UtilityProperties,
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

    companion object {
        private val whitelistOfProviders = listOf("SURFLINE", "BUSY", "AIRTEL", "TIGO", "AIRTELTIGO", "MTN", "VODAFONE",
            "VODAFONE BROADBAND","VODAFONE POSTPAID","DSTV", "GOTV", "LICENCE")
        private val log = LoggerFactory.getLogger(javaClass)
    }


    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?
        try {
            log.debug("Calling Payment service to initiate wallet payment")
            val response = paymentClient
                    .initiateVasPayment(request.country?.name.orEmpty(), getRequest(request, transactionRef))
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
        publishPaymentStatus(request.customerId, transactionRef, status, message.orEmpty())
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode, message)
    }

    private fun getRequest(dto: PaymentDTO, transactionRef: String): VASPaymentClientRequestDTO {
        log.debug("Generating W2W payment request for payment service, transactionRef: $transactionRef")
        val props = paymentProperties.config[dto.country]
        val fromUser = MobifinUser(dto.fromAccountRef.orEmpty())
        val country = dto.country?.name?.toUpperCase()
        val serviceID = "UTILITY_PAYMENT_".plus(country)
        var utility = utilityProperties.utility[dto.country]?.get(dto.transactionAction)
        var productID = utilityProperties.productId[dto.country]?.get(utility)?.get(dto.toAccountCode).toString()
        val service = utilityProperties.service[dto.country]?.get(dto.toAccountName?.replace("Vodafone ", ""))
        val data: VASReqData
        val payment = MobifinPayment(dto.amount.toPlainString(), "0", props?.currencyCode.orEmpty(), "EMONEY")
        var bundle: String? = EMPTY_STRING
        data = if(dto.country == CountryCodes.ghana){
            dataRequestForGhana(dto, utility, productID, bundle, fromUser, serviceID, transactionRef, payment, service)
        } else {
            dataRequestForNamibia(utility, dto, fromUser, serviceID, productID, transactionRef, payment)
        }

        val req = VASPaymentClientRequestDTO(data)
        log.debug("Payment service req for transactionRef: $transactionRef is: $req")
        return req
    }

    private fun dataRequestForNamibia(
        utility: String?,
        dto: PaymentDTO,
        fromUser: MobifinUser,
        serviceID: String,
        productID: String,
        transactionRef: String,
        payment: MobifinPayment
    ): VASReqData {
        val referenceNumber = if (utility == "electricity") dto.toAccountRef.toString() else null
        val bundle = if (dto.transactionAction == TransactionAction.VAS_AIRTIME) "bundle" else null
        val account = if (utility == "netpayments") dto.toAccountRef else null

        return VASReqData(
            fromUser,
            serviceID,
            productID,
            transactionRef,
            referenceNumber,
            dto.message.orEmpty(),
            dto.toAccountCode.orEmpty(),
            listOf(payment),
            utility.toString(),
            bundle,
            dto.email,
            dto.fromAccountRef,
            account
        )
    }

    private fun dataRequestForGhana(
        dto: PaymentDTO,
        utility: String?,
        productID: String,
        bundle: String?,
        fromUser: MobifinUser,
        serviceID: String,
        transactionRef: String,
        payment: MobifinPayment,
        service: String?
    ): VASReqData {
        var utility1 = utility
        var productID1 = productID
        var bundle1 = bundle
        val providerName = dto.toAccountName?.toUpperCase()?.replace(" (4G)", "")
            ?.replace(" BROADBAND","")?.replace(" POSTPAID","")

        if (!whitelistOfProviders.contains(providerName)) {
            throw ApplicationException(ErrorCodes.BAD_REQUEST, "Unsupported provider $providerName")
        }

        if (dto.toAccountCode.orEmpty().toLowerCase() == "vodafone") {
            utility1 = dto.toAccountCode.orEmpty().toLowerCase()
        } else {
            productID1 = utilityProperties.productId[dto.country]?.get(utility1)?.get(providerName).orEmpty()
            bundle1 = if (dto.transactionAction == TransactionAction.VAS_INTERNET) dto.toAccountCode else null
        }
        return VASReqData(
            fromUser,
            serviceID,
            productID1,
            transactionRef,
            dto.toAccountRef.orEmpty(),
            dto.message.orEmpty(),
            if (dto.transactionAction == TransactionAction.VAS_INTERNET) providerName.toString() else dto.toAccountCode.orEmpty(),
            listOf(payment),
            utility1.orEmpty(),
            bundle1,
            dto.email,
            fromUser.userIdentifier,
            null,
            service
        )
    }


}