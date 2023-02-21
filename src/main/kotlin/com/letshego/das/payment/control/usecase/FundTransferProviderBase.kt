package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.*
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionStatus
import com.letshego.das.payment.common.BALANCES
import com.letshego.das.payment.common.EMONEY1
import com.letshego.das.payment.common.WALLET_SERVICE
import com.letshego.das.payment.config.PaymentErrorProperties
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.control.client.NamibiaEFTClient
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.Transactions
import com.letshego.das.payment.entity.dto.PaymentDTO
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import com.letshego.das.payment.entity.dto.WalletUpdateBalanceRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class FundTransferProviderBase(
    private val kongClient: KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    val transactionsRepository: TransactionsRepository,
    open val transactionMapper: PaymentTransactionMapper,
    private val merchantTransactionClient: MerchantTransactionClient,
    namibiaEFTClient: NamibiaEFTClient
) : FundTransferProvider {

    private val log = LoggerFactory.getLogger(javaClass)

    protected val errorMessage: String = "An error occurred while processing your transaction."

    private val transactionCompleteStatus = listOf(PaymentStatus.FAILURE.name, PaymentStatus.SUCCESS.name)

    @Autowired
    private lateinit var paymentStatus: PublishPaymentStatus

    @Autowired
    private lateinit var errorProperties: PaymentErrorProperties

    protected fun publishPaymentStatus(customerId: Long, transactionRef: String, status: String, message: String){
        if(transactionCompleteStatus.contains(status))
            paymentStatus(customerId, transactionRef, status, message)
    }


    private fun addTransaction(dto: PaymentDTO): Transactions {
        val transaction = transactionMapper.toEntity(dto)
        return transactionsRepository.save(transaction)
    }

    private fun updateTransaction(dto: PaymentResponseDTO, transaction: Transactions) {
        transaction.status = when (dto.status){
            PaymentStatus.IN_PROGRESS.name -> TransactionStatus.IN_PROGRESS
            PaymentStatus.SUCCESS.name -> TransactionStatus.COMPLETE
            else -> TransactionStatus.FAILED
        }
        transaction.failureMessage = dto.message
        transaction.paymentTransactionId = dto.transactionId
        transactionsRepository.save(transaction)
    }

    abstract fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO

    @Transactional
    override fun transfer(dto: PaymentDTO): PaymentResponseDTO {
        val transaction = addTransaction(dto)
        val paymentResponse = pay(dto, transaction.paymentRefNumber.orEmpty())
        updateTransaction(paymentResponse, transaction)
        if(paymentResponse.responseCode != "1"){
            paymentResponse.message = errorProperties.errors[paymentResponse.responseCode.replace("-", "")]
                ?: errorMessage
        }
        if (AccountType.WALLET == dto.fromAccountType)
            updateWalletBalance(dto)
        return paymentResponse
    }

    private fun updateWalletBalance(transaction: PaymentDTO){

        when(transaction.country){
            CountryCodes.botswana -> {
                val walletDetailsRequest = toGetWalletAPIForBotswana(
                    transaction.fromAccountRef.orEmpty(),
                    transaction.transactionRef.orEmpty()
                )
                val walletBalanceResponse = merchantTransactionClient.getBotswanaBalance(
                    walletDetailsRequest,
                    CountryCodes.botswana.name,
                    BALANCES,
                    WALLET_SERVICE
                )
                val walletBalance =  walletBalanceResponse.objectValue?.wallet?.availableBalance!!
                val walletUpdateBalanceRequestDTO = WalletUpdateBalanceRequestDTO(transaction.fromAccountRef.orEmpty(),
                    transaction.transactionRef.orEmpty(), walletBalance)
                eWalletTransactionClient.updateBalanceWalletTransaction(walletUpdateBalanceRequestDTO)
            }
            CountryCodes.namibia, CountryCodes.ghana, CountryCodes.nigeria, CountryCodes.mozambique -> {
                val userAccountData = UserAccountData(transaction.fromAccountRef.orEmpty())
                val walletAPIRequestData = GetWalletAPIRequestData(userAccountData)
                val walletDetailsRequest = GetWalletAPIRequest(walletAPIRequestData)

                try{
                    val walletBalanceResponse = kongClient.getWalletBalance(walletDetailsRequest, transaction.country.name.orEmpty())
                    val walletBalance = walletBalanceResponse.data.balanceData.find { x -> x.pouchExternalId == EMONEY1 }!!
                    val walletUpdateBalanceRequestDTO = WalletUpdateBalanceRequestDTO(transaction.fromAccountRef.orEmpty(),
                        transaction.transactionRef.orEmpty(), walletBalance.unusedValueMajor.toBigDecimal())
                    eWalletTransactionClient.updateBalanceWalletTransaction(walletUpdateBalanceRequestDTO)

                } catch (e: Exception){
                    log.error("Error occurred whilst updating wallet balances for ${transaction.customerId} transaction: " +
                            "${transaction.transactionRef}", e)
                }
            }
        }

    }

    fun addBankTransaction(request: PaymentDTO, status: String, message:String, transactionId:String) {
        val transaction = transactionMapper.toEntity(request)
        transaction.status = when (status){
            PaymentStatus.IN_PROGRESS.name -> TransactionStatus.IN_PROGRESS
            PaymentStatus.SUCCESS.name -> TransactionStatus.COMPLETE
            else -> TransactionStatus.FAILED
        }
        transaction.failureMessage = message
        transaction.paymentTransactionId = transactionId
        transactionsRepository.save(transaction)
    }

    private fun toGetWalletAPIForBotswana(phoneNumber: String, paymentRef: String): BotswanaBalanceRequest {
        val userAccountData = UserAccountDataRequest(phoneNumber)
        val walletAPIRequestData = SystemDataRequest(paymentRef)
        val dataRequest = DataRequest(userAccountData, walletAPIRequestData)
        return BotswanaBalanceRequest(dataRequest)
    }
}