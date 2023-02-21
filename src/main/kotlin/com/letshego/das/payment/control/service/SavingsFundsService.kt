package com.letshego.das.payment.control.service

import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.control.usecase.FundTransferProviderBankInternal
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SavingsFundsService(
    private val fundsTransferProviderBankInternal: FundTransferProviderBankInternal
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun transferToDeposits(payment: PaymentDTO): PaymentResponseDTO {
        val paymentResponseDTO = fundsTransferProviderBankInternal.transfer(payment)

        if (PaymentStatus.SUCCESS.name.equals(paymentResponseDTO.status, ignoreCase = true)) {
            return paymentResponseDTO
        }

        throw ApplicationException(
            ErrorCodes.INTERNAL_ERROR,
            paymentResponseDTO.message
        )
    }

}