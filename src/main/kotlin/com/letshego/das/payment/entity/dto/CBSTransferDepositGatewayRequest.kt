package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class CBSTransferDepositGatewayRequest(
    @JsonProperty("from_account_number")
    val fromAccountNumber: String?,

    @JsonProperty("to_account_number")
    val toAccountNumber: String?,

    val amount: BigDecimal?,

    @JsonProperty("transaction_amount")
    val transactionAmount: BigDecimal?,

    @JsonProperty("transaction_currency_code")
    val transactionCurrencyCode: String?,

    @JsonProperty("account_currency_code")
    val accountCurrencyCode: String?,

    @JsonProperty("statement_narration")
    val statementNarration: String?,

    val country: String?
)