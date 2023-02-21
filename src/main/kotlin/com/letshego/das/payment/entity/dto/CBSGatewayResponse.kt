package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CBSGatewayResponse<T>(
    @get:JsonProperty("ErrorMessage") var error: Error?,
    @get:JsonProperty("Data") var data: Data<T>?
) {

    data class Error(
        @get:JsonProperty("SupOvrrd") var supOvrrd: String?,
        @get:JsonProperty("ErrMsg") var message: String?,
        @get:JsonProperty("ErrCode") var code: String?,
    )

    data class Data<T>(
        @get:JsonProperty("OkMessage") var okMessage: T?
    )

}