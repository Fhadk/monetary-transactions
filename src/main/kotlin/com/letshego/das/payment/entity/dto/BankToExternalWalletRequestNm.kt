package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.letshego.das.client.vas.CbsData

data class BankToExternalWalletRequestNm(
    @get:JsonProperty("amount")
    val amount: String,
    @get:JsonProperty("cbs_data")
    val cbsData: CbsData,
    @get:JsonProperty("description")
    val description: String?,
    @get:JsonProperty("ewallet_transactionid")
    val ewalletTransactionId: String,
    @get:JsonProperty("extrnx_code")
    val extrnxCode: String,
    @get:JsonProperty("process_cbs")
    val processCbs: Boolean,
    @get:JsonProperty("recipient")
    val recipient: String,
    @get:JsonProperty("smstext")
    val smstext: String?,
    @get:JsonProperty("wallet")
    val wallet: String,
    @get:JsonProperty("channel")
    val channel: String,
    @get:JsonProperty("callbackUrl")
    val callBackUrl: String
)

data class BankToVASPaymentNm(
    @get:JsonProperty("extrnx_code")
    val extrnxCode: String,
    @get:JsonProperty("meterno")
    val meterNo: String,
    @get:JsonProperty("amount")
    val amount: String,
    @get:JsonProperty("process_cbs")
    val processCbs: Boolean,
    @get:JsonProperty("cbs_data")
    val cbsData: CbsData,
    @get:JsonProperty("ewallet_transactionid")
    val ewalletTransactionId: String,
    @get:JsonProperty("netcode")
    val netCode: String,
    @get:JsonProperty("account")
    val account: String,
    @get:JsonProperty("channel")
    val channel: String,
    @get:JsonProperty("callbackUrl")
    val callBackUrl: String
)
