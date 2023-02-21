package com.letshego.das.payment.entity.dto

import java.math.BigDecimal

data class BankToExternalWalletResponseNm(
    val `data`: DataNm?,
    val status: String
)


data class DataNm(
    val extrnx_code: String?,
    val referencenumber:String?,
    val amount: String?,
    val provider: String?,
    val currency: String?,
    val remark: String?
)