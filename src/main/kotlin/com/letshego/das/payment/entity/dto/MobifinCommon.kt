package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty



class MobifinUser(
        @field:JsonProperty("userIdentifier") val userIdentifier: String
)

class MobifinPayment(

        @field:JsonProperty("amount") val amount: String,
        @field:JsonProperty("exponent") val exponent: String,
        @field:JsonProperty("currency") val currency: String,
        @field:JsonProperty("unitType") val unitType: String

)

