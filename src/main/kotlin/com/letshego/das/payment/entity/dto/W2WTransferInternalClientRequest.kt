package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty

class W2WTransferInternalClientRequest(
        @field:JsonProperty("data") val data: W2WInternalReqData
)

class ExternalW2WTransferClientRequest(
        @field:JsonProperty("data") val data: ExternalW2WRequestData
)

class W2WInternalReqData(
        @field:JsonProperty("fromUser") val fromUser: MobifinUser,
        @field:JsonProperty("toUser") val toUser: MobifinUser,
        @field:JsonProperty("serviceId") val serviceId: String,
        @field:JsonProperty("productId") val productId: String,
        @field:JsonProperty("requestId") val requestId: String,
        @field:JsonProperty("remarks") val remarks: String,
        @field:JsonProperty("payment") val payment: List<MobifinPayment>
)

class ExternalW2WRequestData(
        @field:JsonProperty("toUser") val toUser: MobifinUser,
        @field:JsonProperty("serviceId") val serviceId: String,
        @field:JsonProperty("productId") val productId: String,
        @field:JsonProperty("requestId") val requestId: String,
        @field:JsonProperty("referenceNumber") val referenceNumber: String,
        @field:JsonProperty("remarks") val remarks: String,
        @field:JsonProperty("provider") val provider: String,
        @field:JsonProperty("payment") val payment: List<MobifinPayment>
)

class W2WTransferExternalClientRequest(
        @field:JsonProperty("data") val data: W2WExternalReqData
)

class W2WExternalReqData(
        @field:JsonProperty("fromUser") val fromUser: MobifinUser,
        @field:JsonProperty("referenceNumber") val referenceNumber: String,
        @field:JsonProperty("serviceId") val serviceId: String,
        @field:JsonProperty("productId") val productId: String,
        @field:JsonProperty("requestId") val requestId: String,
        @field:JsonProperty("remarks") val remarks: String,
        @field:JsonProperty("provider") val provider: String,
        @field:JsonProperty("payment") val payment: List<MobifinPayment>
)


