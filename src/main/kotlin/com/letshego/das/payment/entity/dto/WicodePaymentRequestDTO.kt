package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

class WicodePaymentRequestDTO (
    @field:JsonProperty("transaction_type")
    val transactionType: String,
    val wicode: String,
    @field:JsonProperty("account_number")
    val accountNumber: String,
    @field:JsonProperty("process_cbs")
    val processCbs: Boolean,
    @field:JsonProperty("cbs_data")
    val cbsData: MerchantPaymentData
)

class MerchantPaymentData(
    @field:JsonProperty("account_number")
    val accountNumber: String,
    val channel: String
)

class WicodePaymentResponseDTO(
    val status: String?,
    val message: String?,
    val code: Int?,
    val data: WicodePaymentResponseDataDTO
)

class WicodePaymentResponseDataDTO(
    @field:JsonProperty("esb_reference")
    val esbReference: String?,
    val amount: BigDecimal,
    val wicode: String?,
    @field:JsonProperty("account_number")
    val accountNumber: String?
)