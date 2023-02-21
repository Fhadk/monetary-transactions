package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.letshego.das.payment.common.EMONEY
import java.math.BigDecimal

class WalletToBankExternalAPIRequest (
    val data: BaseWalletToBankExternalData
)

class NamibiaWalletToBankExternalAPIRequest(userIdentifier: String,
                                            serviceId: String,
                                            productId: String,
                                            requestId: String,
                                            amount: BigDecimal,
                                            currency: String,
                                            remarks: String,
                                            @field:JsonProperty("benefiaciaryname")
                                            val beneficiaryName: String,
                                            @field:JsonProperty("beneficiaryreference")
                                            val beneficiaryReference: String,
                                            @field:JsonProperty("branchcode")
                                            val branchCode: String,
                                            val referenceNumber: String) :
    BaseWalletToBankExternalData(serviceId, productId, requestId, remarks, userIdentifier, amount, currency) {
}

class GhanaWalletToBankExternalAPIRequest(userIdentifier: String,
                                          serviceId: String,
                                          productId: String,
                                          requestId: String,
                                          amount: BigDecimal,
                                          currency: String,
                                          remarks: String,
                                          @field:JsonProperty("beneficiary_fi_code")
                                          val beneficiaryCode: String,
                                          @field:JsonProperty("beneficiary_account_name")
                                          val beneficiaryAccountName: String,
                                          @field:JsonProperty("beneficiary_account_number")
                                          val beneficiaryAccountNumber: String) :
    BaseWalletToBankExternalData(serviceId, productId, requestId, remarks, userIdentifier, amount, currency){
}

class BankToBankTransferApiRequest(
    @field:JsonProperty("amount")
    val amount: BigDecimal,
    @field:JsonProperty("currency")
    val currency: String,
    @field:JsonProperty("country")
    val country: String,
    @field:JsonProperty("account_number")
    val accountNumber: String,
    @field:JsonProperty("beneficiary_fi_code")
    val beneficiaryCode: String,
    @field:JsonProperty("beneficiary_account_name")
    val beneficiaryAccountName: String,
    @field:JsonProperty("beneficiary_account_number")
    val beneficiaryAccountNumber: String,
    @field:JsonProperty("extrnx_code")
    val extrnxCode: String) {}


sealed class BaseWalletToBankExternalData(val serviceId: String,
                                          val productId: String,
                                          val requestId: String,
                                          val remarks: String,
                                          userIdentifier: String,
                                          amount: BigDecimal,
                                          currency: String) {
    val fromUser = MobifinUser(userIdentifier)
    val payment = MobifinPayment(amount.toPlainString(), "0", currency, EMONEY)
}