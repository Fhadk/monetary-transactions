package com.letshego.das.payment.control.usecase

import com.letshego.das.client.vas.CbsData
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.common.CHANNEL_MALL
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.control.client.*
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.ResponseCode
import com.letshego.das.payment.entity.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FundTransferBankToWalletExternal(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val rubyClient: RubyClient,
    private val cbsClient: CBSClient,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val countryProperties: CountryProperties,
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
    private val log = LoggerFactory.getLogger(javaClass)
    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        return when (request.country) {
            CountryCodes.ghana -> {
                initiateBankToExternalWalletTransferForGhana(request, transactionRef)
            }

            CountryCodes.namibia -> {
                initiateBankToExternalWalletTransferForNamibia(request, transactionRef)
            }

            else ->
                throw ApplicationException(ErrorCodes.COUNTRY_NOT_SUPPORTED)
        }
    }

    private fun initiateBankToExternalWalletTransferForNamibia(
        request: PaymentDTO,
        transactionRef: String
    ): PaymentResponseDTO {
        var status: String
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR
        try {
            log.info("Executing transfer-to-deposit request from kong gateway: $request")
            val response = cbsClient.walletTransferInitiate(
                toBankToExWalletTransferApiRequestForNm(request, transactionRef)
            )

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

    private fun initiateBankToExternalWalletTransferForGhana(
        request: PaymentDTO,
        transactionRef: String
    ): PaymentResponseDTO {
        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR
        try {
            log.info("Executing transfer-to-deposit request from kong gateway: $request")
            val response = rubyClient.bankToExternalWalletTransferInitiate(
                toBankToExternalWalletTransferApiRequestForGh(request, transactionRef)
            )

            checkErrorGh(response)

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


    private fun toBankToExWalletTransferApiRequestForNm(
        request: PaymentDTO,
        transactionRef: String
    ): BankToExternalWalletRequestNm {
        return BankToExternalWalletRequestNm(
            request.amount.toString(),
            CbsData(
                request.fromAccountRef.orEmpty()
            ),
            request.message,
            transactionRef,
            transactionRef,
            true,
            CountryCodes.namibia.phoneCode.plus(request.toAccountRef.orEmpty()).replace("+", ""),
            "",
            request.toAccountCode.orEmpty(),
            CHANNEL_MALL,
            callBackUrl
        )
    }

    private fun toBankToExternalWalletTransferApiRequestForGh(
        request: PaymentDTO,
        transactionRef: String
    ): BankToExternalWalletTransferRequestGh {
        return BankToExternalWalletTransferRequestGh(
            request.amount,
            CbsData(
                request.fromAccountRef.orEmpty(),
            ),
            countryProperties.config[request.country]?.currencyCode.toString(),
            transactionRef,
            true,
            request.toAccountName.orEmpty(),
            request.toAccountRef.orEmpty(),
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

    private fun checkErrorGh(response: BankToExternalWalletResponseGh) {
        if (response.status.equals("fail", true)) {
            throw ApplicationException(
                ErrorCodes.KONG_GATEWAY_ERROR,
                response.status
            )
        }
    }

}