package com.letshego.das.payment.control.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.letshego.das.client.wallet.KongClient
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.common.KONG_METHOD_TRANSFER_TO_DEPOSIT
import com.letshego.das.payment.common.KONG_SERVICE_NAME
import com.letshego.das.payment.control.client.CBSClient
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.ResponseCode
import com.letshego.das.payment.entity.dto.CBSGatewayResponse
import com.letshego.das.payment.entity.dto.CBSTransferDepositGatewayRequest
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FundTransferProviderBankInternal(
    private val cbsClient: CBSClient,
    private val kongClient: KongClient,
    private val objectMapper: ObjectMapper,
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
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
        var message: String? = null
        var responseCode = ResponseCode.ERROR

        try {
            log.info("Executing transfer-to-deposit request from kong gateway: $request")
            val response = cbsClient.cbsCall(
                request.country!!.name,
                KONG_SERVICE_NAME,
                KONG_METHOD_TRANSFER_TO_DEPOSIT,
                toCBSTransferDepositGatewayRequest(request)
            )


            checkError(response)

            log.info("Transaction has been processed successfully from KONG")
            status = PaymentStatus.SUCCESS.name
            transactionId = response.data!!.okMessage!!.rcptData
            message = objectMapper.writeValueAsString(response.data!!.okMessage)
            responseCode = ResponseCode.SUCCESS

        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }

        // publishPaymentStatus(request.customerId, transactionRef, status, message.orEmpty())
        return PaymentResponseDTO(
            status,
            transactionId.orEmpty(),
            transactionRef,
            responseCode.code.toString(),
            message
        )
    }

    private fun toCBSTransferDepositGatewayRequest(request: PaymentDTO): CBSTransferDepositGatewayRequest {
        return CBSTransferDepositGatewayRequest(
            request.fromAccountRef,
            request.toAccountRef,
            request.amount,
            request.amount,
            request.currencyCode,
            request.currencyCode,
            "Internal Transfer",
            request.country!!.name
        )
    }

    private fun <T> checkError(response: CBSGatewayResponse<T>) {
        if (response.error != null) {
            throw ApplicationException(
                ErrorCodes.KONG_GATEWAY_ERROR,
                response.error!!.message
            )
        }
    }
}