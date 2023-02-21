package com.letshego.das.payment.entity.dto

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.TransactionAction

class ProductRequestDTO (
    val country: CountryCodes,
    val transactionAction: TransactionAction,
    val toAccountCode: String?
)