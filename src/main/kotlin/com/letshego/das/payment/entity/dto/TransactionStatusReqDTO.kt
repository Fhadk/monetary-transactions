package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

class TransactionStatusReqDTO(
        val transactionId: String,
        val status: String,
        val amount: BigDecimal,
        val message: String? = null,
        val token: String? = null,
        val units: String? = null
)

class WalletStatusRequest(
        val uuid: String,
        val status: String,
        @field:JsonProperty("trnx_code")
        val transactionCode: String,
        @field:JsonProperty("provider_txid")
        val providerTransactionId: String?,
        @field:JsonProperty("extrnx_code")
        val externalTransactionCode: String,
        @field:JsonProperty("walletno")
        val walletNumber: String?,
        val identifier: String?,
        @field:JsonProperty("utility_type")
        val utilityType: String?,
        val amount: BigDecimal,
        val provider: String,
        @field:JsonProperty("transaction_type")
        val transactionType: String,
        @field:JsonProperty("error_message")
        val errorMessage: String?
)

class WalletStatusResponse(
        val status: String,
        val message: String?
)