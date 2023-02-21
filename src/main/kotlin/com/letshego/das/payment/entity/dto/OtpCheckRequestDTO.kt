package com.letshego.das.payment.entity.dto

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.AccountType
import org.apache.commons.lang3.builder.ToStringBuilder
import java.math.BigDecimal
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class OtpCheckRequestDTO(
        @field:NotNull
        val country: CountryCodes? = null,
        @field:Min(1)
        val customerId: Long = 0,
        @field:NotNull
        val fromAccountType: AccountType? = null,
        @field:NotBlank
        val fromAccountName: String? = null,
        @field:NotNull
        val toAccountType: AccountType? = null,
        @field:NotBlank
        val toAccountName: String? = null,
        @field:Min(1)
        val amount: BigDecimal = BigDecimal.ZERO
) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}

