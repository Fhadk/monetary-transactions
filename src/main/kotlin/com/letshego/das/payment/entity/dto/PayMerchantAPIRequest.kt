package com.letshego.das.payment.entity.dto

import java.math.BigDecimal

data class PayMerchantAPIRequest(
    val transactionDefinitionId: Long,
    val amount: BigDecimal,
    val fields: List<Params>
)

data class Params(
    val name: String,
    val value: String?
)

data class PayMerchantAPIResponse(
    val messageData: List<Response> ? = null,
    val transactionId: Long? = null,
    val errors: List<String>? = null
)

data class Response(
    val title: String,
    val message: String
)