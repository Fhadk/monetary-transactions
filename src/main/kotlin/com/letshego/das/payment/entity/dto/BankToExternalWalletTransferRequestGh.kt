package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.letshego.das.client.vas.CbsData
import java.math.BigDecimal

data class BankToExternalWalletTransferRequestGh(
    @get:JsonProperty("amount")
    val amount: BigDecimal,
    @get:JsonProperty("cbs_data")
    val cbsData: CbsData,
    @get:JsonProperty("currency")
    val currency: String,
    @get:JsonProperty("extrnx_code")
    val extrnxCode: String,
    @get:JsonProperty("process_cbs")
    val processCbs: Boolean,
    @get:JsonProperty("provider")
    val provider: String,
    @get:JsonProperty("walletno")
    val walletNo: String
)

data class CbsDataGh(
    @get:JsonProperty("account_number")
    val account_number: String,
    val channel : String,
    val country : String
)
