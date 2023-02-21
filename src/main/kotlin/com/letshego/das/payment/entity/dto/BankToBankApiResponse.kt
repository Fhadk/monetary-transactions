package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BankToBankApiResponse(
    @get:JsonProperty("status") var status: String,
    @get:JsonProperty("data") var data: BankToBankApiData,
    @get:JsonProperty("message") var message: String?,
    @get:JsonProperty("error_code") var errorCode: String?
) {
    data class BankToBankApiData(
        @get:JsonProperty("uuid") var uuid: String?,
        @get:JsonProperty("status") var status: String?,
        @get:JsonProperty("trnx_code") var trnx_code: String?,
        @get:JsonProperty("provider_txid") var provider_txid: String?,
        @get:JsonProperty("extrnx_code") var extrnx_code: String?,
        @get:JsonProperty("walletno") var walletno: String?,
        @get:JsonProperty("amount") var amount: Any?,
        @get:JsonProperty("provider") var provider: String?,
        @get:JsonProperty("custom_data") var custom_data: Any?,
        @get:JsonProperty("process_cbs") var process_cbs: Boolean?,
        @get:JsonProperty("cbs_txid") var cbs_txid: String?,
        @get:JsonProperty("cbs_error") var cbs_error: Any?,
        @get:JsonProperty("cbs_data") var cbsData: CBSData?
    )

    data class CBSData(
        @get:JsonProperty("account_number") var uuid: String?,
        @get:JsonProperty("beneficiary_account_number") var status: String?,
        @get:JsonProperty("beneficiary_account_name") var trnx_code: String?,
        @get:JsonProperty("country") var provider_txid: String?,
    )
}