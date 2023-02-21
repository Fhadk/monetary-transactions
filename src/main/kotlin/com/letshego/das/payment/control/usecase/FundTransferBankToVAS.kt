package com.letshego.das.payment.control.usecase

import com.letshego.das.client.vas.CbsData
import com.letshego.das.client.vas.HubtleBillPayRequest
import com.letshego.das.client.vas.HubtleBillPayResponse
import com.letshego.das.client.vas.VasClient
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.EMPTY_STRING
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.payment.common.*
import com.letshego.das.payment.config.UtilityProperties
import com.letshego.das.payment.control.client.CBSClient
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.ResponseCode
import com.letshego.das.payment.entity.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FundTransferBankToVAS(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val hubtleVasBillPayClient: VasClient,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val countryProperties: CountryProperties,
    private val cbsClient: CBSClient,
    private val utilityProperties: UtilityProperties,
    private val merchantTransactionClient: MerchantTransactionClient,
    private val namibiaEFTClient: NamibiaEFTClient,
    @Value("\${callback.env}")
    private val callBackUrl: String
) : FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    transactionMapper,
    merchantTransactionClient,
    namibiaEFTClient
) {
    companion object {
        private val whitelistOfVASTransactionAction =
            listOf(
                TransactionAction.VAS_ELECTRICITY,
                TransactionAction.VAS_INTERNET,
                TransactionAction.VAS_BILL_PAYMENT,
                TransactionAction.VAS_AIRTIME,
                TransactionAction.VAS_CABLE_TV,
                TransactionAction.VAS_DATA_BUNDLE,
                TransactionAction.VAS_WATER,
                TransactionAction.VAS_MOBILE_TOPUP,
                TransactionAction.VAS_VOICE_BUNDLE,
            )
        private val log = LoggerFactory.getLogger(javaClass)
    }

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        return when (request.country) {
            CountryCodes.ghana -> {
                payBillForGhana(request, transactionRef)
            }
            CountryCodes.namibia -> {
                initiateVASPaymentForNamibia(request, transactionRef)
            }
            else ->
                throw ApplicationException(ErrorCodes.COUNTRY_NOT_SUPPORTED)
        }
    }


    private fun toHubtleBillPayRequest(request: PaymentDTO): HubtleBillPayRequest {
        if (!whitelistOfVASTransactionAction.contains(request.transactionAction)) {
            throw ApplicationException(ErrorCodes.BAD_REQUEST, "Unsupported Action ${request.transactionAction}")
        }

        val utilityType = when (request.toAccountName) {
            VODAFONE_POSTPAID -> {
                "postpaidbill"
            }
            VODAFONE_BROADBAND -> {
                "internetbill"
            }
            else -> {
                utilityProperties.utility[request.country]?.get(request.transactionAction).orEmpty()
            }
        }
        return HubtleBillPayRequest(
            utilityType,
            request.toAccountRef,
            EMPTY_STRING,
            request.email.orEmpty(),
            request.message,
            request.amount,
           if (request.transactionAction == TransactionAction.VAS_INTERNET || request.transactionAction == TransactionAction.VAS_VOICE_BUNDLE) {
                when (request.toAccountName) {
                    VODAFONE_POSTPAID, VODAFONE_BROADBAND -> {
                        VODAFONE
                    }
                    AIRTELTIGO -> {
                        AIRTELTIGO
                    }
                    else -> request.toAccountName
                }
            } else request.toAccountCode,
            if (request.transactionAction == TransactionAction.VAS_INTERNET || request.transactionAction == TransactionAction.VAS_VOICE_BUNDLE) request.toAccountCode else "",
            request.transactionRef,
            true,
            CHANNEL_DIGITAL_MALL,
            CbsData(request.fromAccountRef)

        )
    }

    fun payBillForGhana(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        val hubtleRequest = toHubtleBillPayRequest(request)
        var status: String
        var transactionId: String? = null
        var message: String?
        var responseCode = ResponseCode.ERROR
        val response: HubtleBillPayResponse?
        try {
            log.info("initiating merchant payment: calling KONG......")
            response = hubtleVasBillPayClient.payVASBills(hubtleRequest)
            println("response.message " + response.message)
            if (response.status.equals(PaymentStatus.SUCCESS.name, ignoreCase = true)) {
                log.info("Transaction has been processed successfully from KONG")
                status = PaymentStatus.SUCCESS.name
                transactionId = request.transactionRef
                message = ""
                responseCode = ResponseCode.SUCCESS
            } else {
                status = PaymentStatus.FAILURE.name
                message = response.message
                transactionId = response.externalReference

            }
        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        this.addBankTransaction(request, status, message.orEmpty(), transactionId.orEmpty())
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode.toString(), message)
    }

    private fun initiateVASPaymentForNamibia(
        request: PaymentDTO,
        transactionRef: String
    ): PaymentResponseDTO {
        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR
        try {
            log.info("Executing transfer-to-deposit request from kong gateway: $request")

            val response =
                if (request.transactionAction == TransactionAction.VAS_ELECTRICITY) cbsClient.namibiaElectricityPayment(

                    bankToElectricityVASRequest(request, transactionRef)
                ) else cbsClient.namibiaVASPayment(bankToVASRequest(request, transactionRef))

            checkErrorNm(response)

            log.info("Transaction has been processed successfully from KONG")
            status = PaymentStatus.SUCCESS.name
            transactionId = transactionRef
            responseCode = ResponseCode.SUCCESS

        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        this.addBankTransaction(request, status, message.orEmpty(), transactionId.orEmpty())
        return PaymentResponseDTO(
            status,
            transactionRef,
            transactionRef,
            responseCode.code.toString(),
            message
        )
    }

    private fun bankToElectricityVASRequest(request: PaymentDTO, transactionRef: String): BankToVASPaymentNm {
        return BankToVASPaymentNm(
            transactionRef,
            request.toAccountRef.orEmpty(),
            request.amount.toString(),
            true,
            CbsData(
                request.fromAccountRef.orEmpty()
            ),
            transactionRef,
            FIN,
            EMPTY_STRING,
            CHANNEL_MALL,
            callBackUrl

        )
    }

    private fun bankToVASRequest(request: PaymentDTO, transactionRef: String): BankToVASPaymentNm {
        return BankToVASPaymentNm(
            transactionRef,
            EMPTY_STRING,
            request.amount.toString(),
            processCbs = true,
            CbsData(request.fromAccountRef.orEmpty()),
            transactionRef,
            request.toAccountCode.orEmpty(),
            request.toAccountRef.orEmpty(),
            CHANNEL_MALL,
            callBackUrl
        )
    }

    private fun checkErrorNm(response: BankToExternalWalletResponseNm) {
        if (response.status.equals("fail", true)) {
            throw ApplicationException(
                ErrorCodes.KONG_GATEWAY_ERROR,
                response.status
            )
        }
    }
}