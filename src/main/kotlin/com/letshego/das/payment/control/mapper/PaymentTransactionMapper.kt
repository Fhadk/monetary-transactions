package com.letshego.das.payment.control.mapper

import com.letshego.das.ms.web.wallet.TransactionStatus
import com.letshego.das.payment.entity.domain.Transactions
import com.letshego.das.payment.entity.dto.PaymentDTO
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring", imports = [TransactionStatus::class])
interface PaymentTransactionMapper {

    @Mappings(
            Mapping(target = "fromAccountRefNumber", source = "dto.fromAccountRef"),
            Mapping(target = "toAccountRefNumber", source = "dto.toAccountRef"),
            Mapping(target = "amount", source = "dto.amount"),
            Mapping(target = "status", expression = "java(TransactionStatus.IN_PROGRESS)"),
            Mapping(target = "paymentRefNumber", source = "dto.transactionRef")
    )
    fun toEntity(dto: PaymentDTO): Transactions
}