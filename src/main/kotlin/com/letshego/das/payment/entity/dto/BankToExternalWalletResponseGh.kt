package com.letshego.das.payment.entity.dto

import java.math.BigDecimal

data class BankToExternalWalletResponseGh(
    val `data`: DataGh,
    val status: String
)

class CustomData(
)

data class DataGh(
    val amount: BigDecimal?,
    val cbs_data: CbsDataGh?,
    val cbs_error: String?,
    val cbs_txid: String?,
    val custom_data: CustomData?,
    val extrnx_code: String?,
    val process_cbs: Boolean?,
    val provider: String?,
    val provider_txid: String?,
    val status: String?,
    val trnx_code: String?,
    val uuid: String?,
    val walletno: String?,
)