package com.letshego.das.payment.entity.domain

import com.letshego.das.ms.jpa.BaseEntity
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionStatus
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "transaction_details")
class Transactions(
        var customerId: Long = 0,
        @Enumerated(EnumType.STRING)
        var fromAccountType: AccountType? = null, // WALLET/BANK_ACCOUNT
        var fromAccountName: String? = null, //WALLET/BANK Name
        var fromAccountRefNumber: String? = null, //wallet identifier / bank account number
        var fromAccountCode: String? = null,
        @Enumerated(EnumType.STRING)
        var toAccountType: AccountType? = null, // WALLET/BANK_ACCOUNT
        var toAccountName: String? = null, //WALLET/BANK Name
        var toAccountRefNumber: String? = null, //wallet identifier / bank account number
        var toAccountCode: String? = null,
        var amount: BigDecimal = BigDecimal.ZERO,
        @Column(unique = true)
        var paymentRefNumber: String? = null,
        @Enumerated(EnumType.STRING)
        var status: TransactionStatus = TransactionStatus.IN_PROGRESS,
        var paymentTransactionId: String? = null,
        var message: String? = null,
        var failureMessage: String? = null,
        var vasToken: String? = null,
        var qrCode: String? = null
) : BaseEntity()