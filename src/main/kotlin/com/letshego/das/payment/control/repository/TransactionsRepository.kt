package com.letshego.das.payment.control.repository

import com.letshego.das.payment.entity.domain.Transactions
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

interface TransactionsRepository : JpaRepository<Transactions, Long> {

    fun findByCustomerIdAndCreatedDateGreaterThan(customerId: Long, date: ZonedDateTime): List<Transactions>

    fun findByPaymentTransactionIdOrPaymentRefNumber(transactionId: String, transactionRef: String): Transactions?
}

