package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.KongClient
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.MerchantField
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.ResponseCode
import com.letshego.das.payment.entity.dto.Params
import com.letshego.das.payment.entity.dto.PayMerchantAPIRequest
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FundTransferProviderExternalP2M (
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    val kongClient: KongClient,
    val eWalletTransactionClient: EWalletTransactionClient,
    private val merchantClient: MerchantClient,
    @Value("\${transaction.definition.id}")
    private val id: Long,
    private val merchantTransactionClient: MerchantTransactionClient,
    private val namibiaEFTClient: NamibiaEFTClient
): FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    transactionMapper,
    merchantTransactionClient,
    namibiaEFTClient
)
{
    private val log = LoggerFactory.getLogger(javaClass)

    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        val kongRequest = toPayMerchantAPIRequest(request)
        var status = PaymentStatus.IN_PROGRESS.name
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR

        try {
            log.info("initiating merchant payment: calling KONG......")
            val payMerchantAPIResponse =  merchantClient.payMerchant(kongRequest, request.country?.name.orEmpty())
            if (payMerchantAPIResponse.messageData!![0].title.equals(PaymentStatus.SUCCESS.name, ignoreCase = true)){
                log.info("Transaction has been processed successfully from KONG")
                status = PaymentStatus.SUCCESS.name
                transactionId = payMerchantAPIResponse.transactionId.toString()
                message = payMerchantAPIResponse.messageData[0].message
                responseCode = ResponseCode.SUCCESS
            }
        } catch (ex: Exception){
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }

        publishPaymentStatus(request.customerId, transactionRef, status, message.orEmpty())
        return PaymentResponseDTO(status, transactionId.orEmpty(), transactionRef, responseCode.toString(), message)
    }

    fun toPayMerchantAPIRequest(dto: PaymentDTO): PayMerchantAPIRequest {
        val isQrCode: Boolean = !dto.qrCode.isNullOrBlank()
        val fields: List<Params> = listOf(
            Params(MerchantField.ACCOUNT_NO.field, dto.fromAccountRef ),
            Params(MerchantField.DESCRIPTION.field, dto.message),
            if(isQrCode) Params(MerchantField.QR_CODE.field, dto.qrCode)
            else Params(MerchantField.USSD_CODE.field, dto.toAccountRef)
        )
        return PayMerchantAPIRequest(id, dto.amount, fields)
    }
}