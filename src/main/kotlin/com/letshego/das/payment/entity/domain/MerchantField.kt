package com.letshego.das.payment.entity.domain

enum class MerchantField(val field: String) {
    ACCOUNT_NO("accountNo"),
    DESCRIPTION("description"),
    QR_CODE("qrcode"),
    USSD_CODE("ussdcode")
}