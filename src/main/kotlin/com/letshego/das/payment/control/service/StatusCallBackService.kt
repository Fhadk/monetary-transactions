package com.letshego.das.payment.control.service

import com.letshego.das.payment.control.usecase.UpdateTransactionStatus
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.TransactionStatusReqDTO
import com.letshego.das.payment.entity.dto.WalletStatusRequest
import com.letshego.das.payment.entity.dto.WalletStatusResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StatusCallBackService(private val updateTransactionStatus: UpdateTransactionStatus) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    operator fun invoke(request: WalletStatusRequest): WalletStatusResponse{
        log.info("Updating Ghana external wallet status: ${request.externalTransactionCode} status: ${request.status}")
        val callBackRequest = TransactionStatusReqDTO(
            request.externalTransactionCode,
            request.status,
            request.amount,
            request.errorMessage
        )
        return try{
            updateTransactionStatus(callBackRequest)
            log.info("Successfully updated GH external wallet: ${request.externalTransactionCode} - ${request.status}")
            WalletStatusResponse(PaymentStatus.SUCCESS.name, "Successfully updated status.")
        } catch (e: Exception){
            log.error("Error updating status for Ghana external wallet".plus(e.toString()))
            WalletStatusResponse(PaymentStatus.FAILURE.name, e.message)
        }
    }
}