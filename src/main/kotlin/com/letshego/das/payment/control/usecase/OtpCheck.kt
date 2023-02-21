package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.context.UseCase
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.config.PaymentProperties
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.Transactions
import com.letshego.das.payment.entity.dto.OtpCheckRequestDTO
import com.letshego.das.payment.entity.dto.OtpRequiredResDTO
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

@UseCase
class OtpCheck(
        private val transactionsRepository: TransactionsRepository,
        private val paymentProperties: PaymentProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    operator fun invoke(dto: OtpCheckRequestDTO): OtpRequiredResDTO {
        log.debug("Checking OTP required for transaction for $dto")
        val lastMonthEnd = ZonedDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).minusDays(1)
        val transactions = transactionsRepository.findByCustomerIdAndCreatedDateGreaterThan(dto.customerId, lastMonthEnd)
        val otpRequired = if (transactions.isEmpty()) {
            log.debug("First time transaction, OTP required: true")
            true
        } else {
            val transactionOtpReq = checkTransactionLimit(dto)
            if (!transactionOtpReq) {
                checkMonthlyLimit(dto, transactions)
            } else transactionOtpReq
        }
        return OtpRequiredResDTO(otpRequired)
    }

    private fun checkTransactionLimit(dto: OtpCheckRequestDTO): Boolean {
        val transactionLimit = paymentProperties.config[dto.country]?.transaction?.limit
        transactionLimit ?: throw ApplicationException(ErrorCodes.INTERNAL_ERROR)
        val transactionKey = dto.fromAccountType?.name?.toLowerCase() + "-" + dto.toAccountType?.name?.toLowerCase()
        val limitValue = transactionLimit[transactionKey]
        log.debug("Transaction Limit value for ${dto.country}: $limitValue")
        val otpRequired = dto.amount > limitValue
        log.debug("OTP required for transaction key: $transactionKey, customerID: ${dto.customerId} is: $otpRequired")
        return otpRequired
    }

    private fun checkMonthlyLimit(dto: OtpCheckRequestDTO, transactions: List<Transactions>): Boolean {
        val transactionLimit = paymentProperties.config[dto.country]?.monthly?.limit
        transactionLimit ?: throw ApplicationException(ErrorCodes.INTERNAL_ERROR)
        val transactionKey = dto.fromAccountType?.name?.toLowerCase() + "-" + dto.toAccountType?.name?.toLowerCase()
        val limitValue = transactionLimit[transactionKey]

        log.debug("Limit value for monthly transaction limit for ${dto.country}, is: $limitValue")
        var totalTransactionAmount = transactions.sumByDouble { it.amount.toDouble() }.toBigDecimal()
        log.debug("Total transaction value for customerID: ${dto.customerId}: $totalTransactionAmount")
        val limitCount = totalTransactionAmount.divide(limitValue)
        totalTransactionAmount = totalTransactionAmount.minus(limitCount.multiply(limitValue))
        log.debug("Refreshed transaction limit")

        val otpRequired = totalTransactionAmount.plus(dto.amount) > limitValue
        log.info("OTP required for transaction key: $transactionKey, customerID: ${dto.customerId} is: $otpRequired")
        return otpRequired
    }
}