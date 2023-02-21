package com.letshego.das.payment.entity.dto

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionAction
import org.apache.commons.lang3.builder.ToStringBuilder
import java.math.BigDecimal
import java.time.ZonedDateTime
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class PaymentDTO(
        @field:Min(1)
        val customerId: Long = 0,
        @field:NotNull
        val country: CountryCodes? = null,
        @field:NotNull
        val fromAccountType: AccountType? = null,
        @field:NotBlank
        val fromAccountName: String? = null,
        @field:NotBlank
        val fromAccountRef: String? = null,
        val fromAccountCode: String? = null,
        val transactionAction: TransactionAction? = null,

        @field:NotNull
        val toAccountType: AccountType? = null,
        @field:NotBlank
        val toAccountName: String? = null,
        @field:NotBlank
        val toAccountRef: String? = null,
        val toAccountCode: String? = null,
        @field:NotBlank
        val transactionRef: String? = null,
        @field:DecimalMin(value="0.01", message="invalid value amount")
        val amount: BigDecimal = BigDecimal.ZERO,
        val message: String? = null,
        val email: String? = null,
        var wicodeTransactionType: String? = null,
        val qrCode: String? = null,
        var transactionTime: ZonedDateTime? = null,
        val balance: BigDecimal = BigDecimal.ZERO,
        val currencyCode: String? = null,
        val beneficiaryReference: String? = null
) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}

