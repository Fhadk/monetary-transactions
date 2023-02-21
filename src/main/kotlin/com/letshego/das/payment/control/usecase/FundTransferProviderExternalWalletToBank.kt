package com.letshego.das.payment.control.usecase

import com.letshego.das.client.vas.CbsData
import com.letshego.das.ms.EMPTY_STRING
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.payment.control.client.*
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.ResponseCode
import com.letshego.das.payment.entity.dto.BankToExternalWalletTransferRequestGh
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FundTransferProviderExternalWalletToBank(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val rubyClient: RubyClient,
    private val countryProperties: CountryProperties,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val merchantTransactionClient: MerchantTransactionClient,
    private val namibiaEFTClient: NamibiaEFTClient
):
    FundTransferProviderBase(
        kongClient,
        eWalletTransactionClient,
        transactionsRepository,
        transactionMapper,
        merchantTransactionClient,
        namibiaEFTClient
    ) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status: String
        var transactionId: String? = null
        var responseCode = "500"
        var message: String?

        try{
            log.debug("Calling Ruby Gateway to initiate external wallet to bank transaction ${request.transactionRef} for ${request.customerId}")
            val response = rubyClient.initiateExternalWalletTobankTransfer(createRequestBody(request))

            if(response.status.equals("fail", true)){
                status = PaymentStatus.FAILURE.name
                message = response.status

            } else{
                log.info("Transaction ${request.transactionRef} for ${request.customerId} has been processed successfully from KONG")
                status = PaymentStatus.SUCCESS.name
                transactionId = transactionRef
                responseCode = ResponseCode.SUCCESS.name
                message = "Successfully Processed"
            }
        } catch (e: Exception){
            log.error("Payment for transactionRef: $transactionRef, FAILED", e)
            status = PaymentStatus.FAILURE.name
            message = e.message
        }

        this.addBankTransaction(request, status, message.orEmpty(), transactionId.orEmpty())
        return PaymentResponseDTO(
            status,
            transactionRef,
            transactionRef,
            responseCode,
            message
        )
    }

    private fun createRequestBody(request: PaymentDTO): BankToExternalWalletTransferRequestGh {
        return BankToExternalWalletTransferRequestGh(
            request.amount,
            CbsData(
                request.toAccountRef
            ),
            countryProperties.config[request.country]?.currencyCode.toString(),
            request.transactionRef.orEmpty(),
            true,
            request.fromAccountCode.orEmpty(),
            request.fromAccountRef.orEmpty()
        )
    }
}