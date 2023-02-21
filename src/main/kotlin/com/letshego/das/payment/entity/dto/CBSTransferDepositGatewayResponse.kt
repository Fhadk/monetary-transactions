package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * This data class represents structure of "OkMessage"
 * {
 *   "Data": {
 *     "OkMessage": {
 *        "AcctNum": "407469",
 *        "Filler2": "O.K.  001",
 *        "RcptData": "O.K.  001407469",
 *        "Filler1": "0000",
 *        "CustNum": "407469"
 *     }
 *   }
 * }
 */

data class CBSTransferDepositGatewayResponse(
    @JsonProperty("AcctNum")
    val accountNumber: String,

    @JsonProperty("Filler2")
    val filler2: String,

    @JsonProperty("RcptData")
    val rcptData: String,

    @JsonProperty("Filler1")
    val filler1: String,

    @JsonProperty("CustNum")
    var custNum: String
)