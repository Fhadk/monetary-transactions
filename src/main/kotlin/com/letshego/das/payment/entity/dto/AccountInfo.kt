package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty

class AccountInfoRequest(
    @field:JsonProperty("account_number")
    val accountNumber: String
)

class AccountInfoResponse(
    @field:JsonProperty("Data")
    val data: AccountInfoResponseData
)

class AccountInfoResponseData(
    @field:JsonProperty("Coll")
    val accounts: List<Account>
)

class Account(
    @field:JsonProperty("AcctSubTyp")
    val accountSubType: String,
    @field:JsonProperty("CreationDt")
    val creationDate: String?,
    @field:JsonProperty("CurCode2")
    val currencyCode: String,
    @field:JsonProperty("Stat")
    val status: String,
    @field:JsonProperty("ProdDescptn")
    val productDescription: String,
    @field:JsonProperty("AcctTyp")
    val accountType: String,
    @field:JsonProperty("Bal")
    val balance: String,
    @field:JsonProperty("AcctNum")
    val accountNumber: String
)