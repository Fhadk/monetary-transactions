package com.letshego.das.payment.entity.dto

class PaymentResponseDTO(
        val status: String,
        val transactionId: String,
        val paymentRef: String,
        val responseCode: String,
        var message: String? = null
)

