package com.letshego.das.payment.entity.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

class InternalBankToBankApiResponse(@field:JsonProperty("nPBTransferToDepositAccountResponse")
                                     val nPBTransferToDepositAccountResponse: PBTransferToDepositAccountResponse) {

    data class PBTransferToDepositAccountResponse(@field:JsonProperty("XferToDepAcctRs")
                                          val xferToDepAcctRs: XferToDepAcctRs) {}

    data class XferToDepAcctRs(@field:JsonProperty("RsHeader")
                               val rqHeader: RsHeader?,
                               @field:JsonProperty("Stat")
                               val stat: ResponseData?) {}

    data class RsHeader(@field:JsonProperty("JrnlNum")
                        val jrnlNum: String?,
                        @field:JsonProperty("RsHdrDt")
                        val rsHdrDt: String?,
                        @field:JsonProperty("OutputType")
                        val outputType: String?) {}

    data class ResponseData(@field:JsonProperty("OkMessage")
                           val okMessage: OkMessageResponse?,
                           @field:JsonProperty("ErrorMessage")
                           val errorMessage: ErrorMessageResponse?) {}

    data class OkMessageResponse(@field:JsonProperty("Filler1")
                                 val filler1: String?,
                                 @field:JsonProperty("Filler2")
                                 val filler2: String?,
                                 @field:JsonProperty("RcptData")
                                 val rcptData: String?,
                                 @field:JsonProperty("CustNum")
                                 val custNum: String?) {}

    data class ErrorMessageResponse(@field:JsonProperty("ErrMsg")
                                    val errMsg: String?,
                                    @field:JsonProperty("SupOvrrd")
                                    val supOvrrd: String?,
                                    @field:JsonProperty("ErrCode")
                                    val errCode: String?) {}
}