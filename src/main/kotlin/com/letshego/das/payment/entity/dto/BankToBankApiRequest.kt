package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BankToBankApiRequest(@get:JsonProperty("nPBTransferToDepositAccount")
                                     val nPBTransferToDepositAccount: PBTransferToDepositAccount) {

    data class PBTransferToDepositAccount(@get:JsonProperty("XferToDepAcctRq")
                                          val xferToDepAcctRq: XferToDepAcctRq) {}

    data class XferToDepAcctRq(@get:JsonProperty("RqHeader")
                               val rqHeader: RqHeader,
                               @field:JsonProperty("Data")
                               val data: RequestData) {}

    data class RqHeader(@get:JsonProperty("UUIDSOURCE")
                        val uuidSource: String,
                        @get:JsonProperty("UUIDNUM")
                        val uuidNum: String,
                        @get:JsonProperty("UUIDSEQNUM")
                        val uuidSeqNum: String) {}

    data class RequestData(@get:JsonProperty("Amt")
                           val amount: String,
                           @get:JsonProperty("TrnAmt")
                           val trnAmt: String,
                           @get:JsonProperty("TrnCurCode")
                           val currency: String,
                           @get:JsonProperty("FrmAcctNum")
                           val fromAccount: String,
                           @get:JsonProperty("PromoNum")
                           val promoNum: String,
                           @get:JsonProperty("ToAcctNum")
                           val toAcctNum: String,
                           @get:JsonProperty("StmtNarr")
                           val stmtNarr: String) {}
}