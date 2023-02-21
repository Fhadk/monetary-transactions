package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.builder.ToStringBuilder

class VASPaymentClientRequestDTO(
        val data: VASReqData
) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}

data class VASReqData(
        val fromUser: MobifinUser,

        val serviceId: String,
        val productId: String,

        @field:JsonProperty("requestId")
        val requestID: String,

        val referenceNumber: String?,
        val remarks: String,
        val provider: String,
        val payment: List<MobifinPayment>,
        val utility: String,
        val bundle: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val account: String? = null,
        val service: String? = null
)


