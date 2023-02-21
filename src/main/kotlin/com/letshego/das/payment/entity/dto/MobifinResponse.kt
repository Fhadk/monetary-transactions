package com.letshego.das.payment.entity.dto

class MobifinResponse (
    val responseCode: String,
    val responseMessage: String,
    val data: MobifinResponseData
)

class MobifinResponseData(
    val transactionId: String
)