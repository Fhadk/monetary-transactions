package com.letshego.das.payment.entity.dto


class BankToWalletInternalAPIRequest(
        val data: BankToWalletData
)

class BankToWalletData(
        val toUser: MobifinUser,
        val referenceNumber: String,
        val requestId: String,
        val payment: MobifinPayment,
        val serviceId: String,
        val productId: String,
        val remarks: String
)