package com.letshego.das.payment.control.service

import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.config.UtilityProperties
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.control.usecase.FundTransferBankToBank
import com.letshego.das.payment.control.usecase.FundTransferBankToWalletExternal
import com.letshego.das.payment.control.usecase.FundTransferBankToVAS
import com.letshego.das.payment.control.usecase.FundTransferFactory
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.ProductRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentsService(
    private val fundTransferFactory: FundTransferFactory,
    private val transactionsRepository: TransactionsRepository,
    private val transactionMapper: PaymentTransactionMapper,
    private val utilityProperties: UtilityProperties,
    private val fundTransferBankToBank: FundTransferBankToBank,
    private val fundTransferBankToVAS: FundTransferBankToVAS,
    private val fundTransferBankToWalletExternal: FundTransferBankToWalletExternal
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun transferFunds(payment: PaymentDTO): PaymentResponseDTO {
        log.info("Initiating wallet to wallet internal payment: $payment")
        val transferImpl = fundTransferFactory.transferProviderFor(payment.fromAccountType, payment.fromAccountName, payment.toAccountType,
            payment.toAccountName, payment.toAccountCode, payment.transactionAction)
        return transferImpl.transfer(payment)
    }

    fun getTransactionRef(dtoPayment: PaymentDTO): String {
        val transaction = transactionsRepository.save(transactionMapper.toEntity(dtoPayment))
        return  transaction.paymentRefNumber.orEmpty()
    }

    fun getProductInformation(product: ProductRequestDTO): String {
        val utility = utilityProperties.utility[product.country]?.get(product.transactionAction)
        return utilityProperties.productId[product.country]?.get(utility)?.get(product.toAccountCode).toString()
    }

    fun doBankTransfer(payment: PaymentDTO): PaymentResponseDTO {
        return if (isBanktoBankTransaction(payment)) {
            fundTransferBankToBank.pay(payment, payment.transactionRef!!)
        } else if (isBankToExternalWalletTransfer(payment)) {
            fundTransferBankToWalletExternal.pay(payment, payment.transactionRef!!)
        } else if (isExternalWalletToBankTransaction(payment)) {
            fundTransferBankToWalletExternal.pay(payment, payment.transactionRef!!)
        } else if (isVASTransaction(payment)){
            fundTransferBankToVAS.pay(payment, payment.transactionRef!!)
        }
        else throw  ApplicationException(ErrorCodes.BAD_REQUEST, "Not Supported Request!!")
    }

    private fun isExternalWalletToBankTransaction(payment: PaymentDTO) =
        payment.fromAccountType == AccountType.WALLET && payment.toAccountType == AccountType.BANK_ACCOUNT

    private fun isVASTransaction(payment: PaymentDTO) =
        payment.toAccountType == AccountType.VAS_ACCOUNT

    private fun isBankToExternalWalletTransfer(payment: PaymentDTO) =
        payment.toAccountType == AccountType.WALLET && payment.transactionAction == TransactionAction.BANK_TO_EXTERNAL_WALLET

    private fun isBanktoBankTransaction(payment: PaymentDTO) =
        payment.toAccountType == AccountType.BANK_ACCOUNT && payment.transactionAction == TransactionAction.BANK_P2P_TRANSFER
}

