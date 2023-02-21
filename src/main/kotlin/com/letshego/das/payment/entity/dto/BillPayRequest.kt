package com.letshego.das.payment.entity.dto

import com.letshego.das.client.vas.CbsData
import com.letshego.das.ms.web.wallet.TransactionAction
import java.math.BigDecimal

class BillPayRequest(

    val utilityType: TransactionAction?,

    val identifier: String?,

    val msisdn: String?,

    val email: String?,

    val narration: String?,

    val amount: BigDecimal?,

    val provider: String?,

    val bundle: String?,

    val externalReference: String?,

    val processCbs: Boolean?,

    val cbsData: CbsData?,
    )

