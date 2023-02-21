package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.builder.ToStringBuilder

class WalletTransferInternalResponse(
        @field:JsonProperty("data") val data: W2WInternalResponse?,
        @field:JsonProperty("responseMessage") val responseMessage: String,
        @field:JsonProperty("responseCode") val responseCode: Int
) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}

class W2WInternalResponse(
        @field:JsonProperty("transactionId") val transactionId: String
)