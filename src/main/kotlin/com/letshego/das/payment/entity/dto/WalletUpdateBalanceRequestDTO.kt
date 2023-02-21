package com.letshego.das.payment.entity.dto

import org.apache.commons.lang3.builder.ToStringBuilder
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

class WalletUpdateBalanceRequestDTO(
        @field:NotNull
        val fromAccount: String,
        @field:NotNull
        val paymentTransactionRef: String,
        @field:NotNull
        val balance: BigDecimal
)