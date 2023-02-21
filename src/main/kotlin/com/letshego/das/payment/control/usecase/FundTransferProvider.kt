package com.letshego.das.payment.control.usecase

import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import com.letshego.das.payment.entity.dto.PaymentDTO
import org.springframework.transaction.annotation.Transactional

interface FundTransferProvider {

    @Transactional
    fun transfer(dto: PaymentDTO) : PaymentResponseDTO

}

