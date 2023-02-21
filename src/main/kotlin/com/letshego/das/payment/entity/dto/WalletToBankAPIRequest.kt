package com.letshego.das.payment.entity.dto

class WalletToBankAPIRequest (
    val data: WalletToBankData
)

class WalletToBankData(
    val fromUser: MobifinUser,
    val referenceNumber: String,
    val requestId: String,
    val payment: MobifinPayment,
    val serviceId: String,
    val productId: String,
    val remarks: String
)