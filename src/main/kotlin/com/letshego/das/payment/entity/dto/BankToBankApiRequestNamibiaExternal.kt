package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.letshego.das.ms.EMPTY_STRING

data class BankToBankApiRequestNamibiaExternal(
        val id : String? = "2",
        @get:JsonProperty("jsonrpc")
        val jsonRPC: String? = "2.0",
        val method: String? = "apiGateway.eeft.post",
        val params: Param
)

data class Param(
        @get:JsonProperty("uuidnumber")
        val uuidNumber: String,
        @get:JsonProperty("uuidsource")
        val uuidSource: String = "MAL",
        @get:JsonProperty("fromaccount")
        val fromAccount: String,
        @get:JsonProperty("statementnarrative")
        val statementNarrative: String? = "External Funds trf",
        @get:JsonProperty("amount")
        val amount: String,
        @get:JsonProperty("currency")
        val currency: String =  "NAD",
        @get:JsonProperty("benefiaciaryname")
        val benefiaciaryName: String?,
        @get:JsonProperty("beneficiaryreference")
        val beneficiaryReference: String?,
        @get:JsonProperty("receivingnibaccount")
        val receivingNibAccount: String? = EMPTY_STRING,
        @get:JsonProperty("promonum")
        val promoNum: String? = EMPTY_STRING,
        @get:JsonProperty("transferamount")
        val transferAmount: String,
        @get:JsonProperty("transfercurrency")
        val transferCurrency: String = "NAD",
        @get:JsonProperty("receivingaccount")
        val receivingAccount: String,
        @get:JsonProperty("beneficiaryaccounttype")
        val beneficiaryAccountType: String? = EMPTY_STRING,
        @get:JsonProperty("bankcode")
        val bankCode: String? = EMPTY_STRING,
        @get:JsonProperty("branchcode")
        val branchCode: String,
        @get:JsonProperty("paymenttype")
        val paymentType: String? = EMPTY_STRING,
        @get:JsonProperty("irdregistrationofficecode")
        val irdRegistrationOfficeCode: String? = EMPTY_STRING,
        @get:JsonProperty("tin")
        val tin: String? = EMPTY_STRING,
        @get:JsonProperty("taxtype")
        val taxType: String? = EMPTY_STRING,
        @get:JsonProperty("taxperiod")
        val taxPeriod: String? = EMPTY_STRING,
        @get:JsonProperty("taxpayertype")
        val taxPayerType: String? = EMPTY_STRING,
        @get:JsonProperty("entryclasscode")
        val entryClassCode: String = "82",
        @get:JsonProperty("servicetype")
        val servicetype: String = "NC",
        @get:JsonProperty("extrnx_code")
        val extrnxCode: String
)