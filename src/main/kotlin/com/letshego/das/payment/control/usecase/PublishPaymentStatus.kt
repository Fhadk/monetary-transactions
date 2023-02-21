package com.letshego.das.payment.control.usecase

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.letshego.das.client.notification.*
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.client.wallet.*
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.EMPTY_STRING
import com.letshego.das.ms.config.NotificationProperties
import com.letshego.das.ms.context.UseCase
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.PaymentStatus
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.ms.web.wallet.TransactionStatus
import com.letshego.das.payment.common.*
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.client.MerchantTransactionClient
import com.letshego.das.payment.entity.dto.PaymentDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.math.BigDecimal
import java.text.MessageFormat
import java.time.format.DateTimeFormatter

@UseCase
class PublishPaymentStatus(
    private val client: AmazonSNS,
    private val objectMapper: ObjectMapper,
    @Value("\${aws.sns.payment-status.topic}")
    private val paymentStatusTopic: String,
    private val notificationClient: NotificationClient,
    private val notificationProperties: NotificationProperties,
    private val kongClient: KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val registrationClient: RegistrationClient,
    private val merchantTransactionClient: MerchantTransactionClient,
    @Value("\${wallet.status}")
    private val walletStatus: String

) {

    private val log = LoggerFactory.getLogger(javaClass)

    operator fun invoke(
        customerId: Long, paymentRef: String, status: String, message: String? = null,
        token: String? = null
    ) {
        //Let's send a notification for successful transactions only
        if (status == PaymentStatus.SUCCESS.name || status == TransactionStatus.COMPLETE.name) {
            val data = PaymentStatusMessageDTO(customerId, paymentRef, status, message, token)
            val request = PublishRequest()
            val body = objectMapper.writeValueAsString(data)
            log.debug("Message content: $body")
            request.targetArn = paymentStatusTopic
            request.message = body
            sendMessage(
                paymentRef,
                Kind.IMMEDIATE,
                customerId,
                message(Channels.PUSH, PaymentDTO(), WalletMessageType.W2W_TRANSACTION),
                Channels.PUSH
            )
            sendMessage(paymentRef, Kind.IMMEDIATE, customerId, message, Channels.IN_APP)
            sendCreditDebitNotification(paymentRef)
        }
    }

    companion object {
        const val PUSH = "push"
        const val DEBIT = "DEBIT"
        const val CREDIT = "CREDIT"
        var FROM_TO = EMPTY_STRING
        var TRANSACTION_TYPE = EMPTY_STRING
        const val ACCOUNT_TYPE = "BANK_ACCOUNT"
    }

    private fun sendCreditDebitNotification(paymentRef: String) {
        val transactionDetailDTOs = eWalletTransactionClient.getWallet(paymentRef).data
        transactionDetailDTOs?.forEach {
            when(it?.fromAccountType){
                AccountType.WALLET -> sendWalletNotification(it,paymentRef)
            }
        }
    }

    private fun sendWalletNotification(it: TransactionDetailDTO?, paymentRef: String) {
        val customerData = registrationClient.getCustomer(it?.id!!).data
        var customerName = EMPTY_STRING
        var accountNumber = EMPTY_STRING
        when (it.transactionType) {
            CREDIT -> {
                customerName = "${customerData?.firstName ?: EMPTY_STRING} ${customerData?.lastName ?: EMPTY_STRING}"
                accountNumber = it.accountName ?: EMPTY_STRING
            }
            DEBIT -> {
                customerName = it.accountName ?: EMPTY_STRING
                accountNumber = it.accountRef ?: EMPTY_STRING
            }
        }
        val paymentDTO = PaymentDTO(
            it.id,
            CountryCodes.fromPhoneCode(customerData?.countryCode.orEmpty()),
            it.accountType,
            null,
            customerData?.phoneNumber,
            null,
            TransactionAction.valueOf(it.transactionAction),
            it.accountType,
            customerName,
            encapsulateAccountNumber(
                it.accountType?.name!!,
                CountryCodes.fromPhoneCode(customerData?.countryCode.orEmpty())?.name,
                accountNumber
            ),
            null,
            it.paymentTransactionRef,
            it.amount,
            null,
            null,
            it.transactionType,
            null,
            it.transactionTime,
            getWalletBalance(customerData?.phoneNumber.orEmpty(), customerData?.countryCode,paymentRef)
        )
        sendMessage(
            paymentRef,
            Kind.IMMEDIATE,
            it.id,
            message(Channels.SMS, paymentDTO, WalletMessageType.W2W_TRANSACTION),
            Channels.SMS,
            null,
            null,
            customerData?.phoneNumber,
            customerData?.countryCode
        )
    }

    fun message(channel: Channels, payment: PaymentDTO, messageType: WalletMessageType): String {
        val template = notificationProperties.template["internal-wallet-in-app"]
        val constants = notificationProperties.constant[payment.country]
        when (payment.wicodeTransactionType) {
            DEBIT -> {
                TRANSACTION_TYPE = "debited"
                FROM_TO = "to"
            }
            CREDIT -> {
                TRANSACTION_TYPE = "credited"
                FROM_TO = "from"
            }
        }
        return when (channel) {
            Channels.PUSH -> objectMapper.writeValueAsString(
                PushMessage(
                    "You have received Funds", MessageFormat.format(
                        template?.get(PUSH)!!,
                        constants?.currency,
                        payment.amount,
                        payment.fromAccountRef,
                    ), messageType.name
                )
            )
            Channels.SMS -> objectMapper.writeValueAsString(
                PushMessage(
                    "Dear Customer ", MessageFormat.format(
                        template?.get(payment.country?.name!!)!!,
                        payment.fromAccountRef,
                        TRANSACTION_TYPE,
                        payment.transactionTime?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                        payment.amount,
                        FROM_TO,
                        "${payment.toAccountName} ${payment.toAccountRef}",
                        payment.balance
                    ), messageType.name
                )
            )
            else -> throw  IllegalStateException("Channel $channel is not supported")
        }
    }

    private fun sendMessage(
        transactionReferenceNumber: String,
        deliveryKind: Kind? = Kind.IMMEDIATE,
        profileId: Long,
        messageBody: String?,
        channels: Channels,
        delay: BigDecimal? = BigDecimal.ZERO,
        interval: BigDecimal? = BigDecimal.ZERO,
        phoneNumber: String? = null,
        countryCode: String? = null
    ) {
        val notificationOrder = NotificationOrder(
            kind = deliveryKind!!,
            channel = channels,
            interval = interval,
            delay = delay,
            message = Message(
                identifier = WalletIdentifier(
                    transactionReferenceNumber,
                    phoneNumber,
                    MessageTypeStatus.WalletMessage(WalletMessageType.W2W_TRANSACTION, MessageStatus.SUCCESSFUL)
                ),
                recipient = profileId.toString(),
                countryCode = countryCode,
                subject = LETSHEGO_WALLET_TRANSACTION,
                body = messageBody.orEmpty()
            )
        )
        log.info("sending $channels for ${notificationOrder.message?.identifier?.value()}")
        notificationClient.notify(notificationOrder)
    }

    private fun toGetWalletAPI(mobileNumber: String): GetWalletAPIRequest {
        val userAccountData = UserAccountData(mobileNumber)
        val walletAPIRequestData = GetWalletAPIRequestData(userAccountData)

        return GetWalletAPIRequest(walletAPIRequestData)
    }

    private fun toGetWalletAPIForBotswana(phoneNumber: String, paymentRef: String): BotswanaBalanceRequest {
        val userAccountData = UserAccountDataRequest(phoneNumber)
        val walletAPIRequestData = SystemDataRequest(paymentRef)
        val dataRequest = DataRequest(userAccountData, walletAPIRequestData)
        return BotswanaBalanceRequest(dataRequest)
    }

    private fun getWalletBalance(mobileNumber: String, countryCode: String?, paymentRef: String): BigDecimal {
        return when (CountryCodes.fromPhoneCode(countryCode!!)?.name!!) {
            CountryCodes.botswana.name -> {
                val walletDetailsRequest = toGetWalletAPIForBotswana(mobileNumber, paymentRef)
                val walletBalanceResponse = merchantTransactionClient.getBotswanaBalance(
                    walletDetailsRequest,
                    CountryCodes.botswana.name,
                    BALANCES,
                    WALLET_SERVICE
                )
                 walletBalanceResponse.objectValue?.wallet?.availableBalance!!
            }
            CountryCodes.namibia.name, CountryCodes.ghana.name, CountryCodes.nigeria.name, CountryCodes.mozambique.name -> {
                when (walletStatus) {
                    "up" -> {
                        val walletDetailsRequest = toGetWalletAPI(mobileNumber)
                        val walletBalanceResponse = kongClient.getWalletBalance(
                            walletDetailsRequest,
                            CountryCodes.fromPhoneCode(countryCode)?.name.orEmpty()
                        )
                            walletBalanceResponse.data.balanceData.find { x -> x.pouchExternalId == EMONEY1 }?.unusedValueMajor!!.toBigDecimal()
                    }
                    else -> BigDecimal(EMPTY_STRING)
                }
            }
            else -> {
                throw ApplicationException(ErrorCodes.COUNTRY_NOT_SUPPORTED)
            }
        }

    }

    private fun encapsulateAccountNumber(accountType: String, country: String?, accountRef: String): String {
        return if (accountRef.length > 7 && accountType.equals(
                ACCOUNT_TYPE,
                true
            ) && country.equals(CountryCodes.mozambique.name)
        ) {
            accountRef.replaceRange(0, 6, "*******").toString()
        } else if (accountRef.length > 6 && accountType.equals(ACCOUNT_TYPE, true)) {
            accountRef.replaceRange(0, 5, "******").toString()
        } else {
            accountRef
        }
    }
}

class PushMessage(val title: String, val description: String, val type: String)