package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

class BuddyPaymentRequestDTO (
    @field:JsonProperty("transaction_id")
    val transactionId: String,
    @field:JsonProperty("transaction_type")
    val transactionType: String,
    val amount: BigDecimal,
    val currency: String,
    @field:JsonProperty("supplier_id")
    val supplierId: String,
    @field:JsonProperty("process_cbs")
    val processCbs: Boolean,
    val customer: BuddyCustomerData,
    @field:JsonProperty("cbs_data")
    val cbsData: MerchantPaymentData
)

class BuddyCustomerData(
    val location: BuddyCustomerLocation?,
    val name: String,
    val msisdn: String,
)

class BuddyCustomerLocation(
    val lat: String?,
    val long: String?
)

class BuddyPaymentResponseDTO(
    val status: String?,
    val message: String?,
    val code: Int?,
    val data: BuddyPaymentRequestDTO?,
    @field:JsonProperty("cbs_txid")
    val cbsTransactionId: String?,
    @field:JsonProperty("cbs_error")
    val cbsError: String?
)