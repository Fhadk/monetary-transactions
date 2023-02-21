package com.letshego.das.payment.control.usecase

import com.letshego.das.client.wallet.PaymentStatusMessageDTO
import com.letshego.das.client.notification.*
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.ms.config.NotificationProperties
import com.letshego.das.ms.web.APIResult
import com.letshego.das.ms.web.wallet.TransactionStatus
import com.letshego.das.payment.common.LETSHEGO
import com.letshego.das.payment.common.TOKEN
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.TransactionStatusReqDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.text.MessageFormat

@Component
class UpdateTransactionStatus(
        private val transactionsRepository: TransactionsRepository,
        private val paymentStatus: PublishPaymentStatus,
        private val notificationClient: NotificationClient,
        private val registrationClient: RegistrationClient,
        private val notificationProperties: NotificationProperties,
        private val eWalletTransactionClient: EWalletTransactionClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    operator fun invoke(dto: TransactionStatusReqDTO){
        log.info("Updating status for transactionID: ${dto.transactionId}, status received: ${dto.status}")
        val transaction = transactionsRepository
            .findByPaymentTransactionIdOrPaymentRefNumber(dto.transactionId, dto.transactionId)
        log.debug("Received transaction object: $transaction for transactionId: ${dto.transactionId}")
        transaction?.apply {
            if(PaymentStatus.SUCCESS.name.equals(dto.status, true)){
                this.status = TransactionStatus.COMPLETE

                //If there's a token, send SMS to customer
                if(!dto.token.isNullOrBlank()){
                    val customer = registrationClient.getCustomer(transaction.customerId)
                    val customerData = customer.data
                    sendSms(customerData?.phoneNumber.orEmpty(),
                            customerData?.countryCode.orEmpty(),
                            transaction.amount,
                            dto.token,
                            dto.units.orEmpty(),
                            transaction.toAccountRefNumber.orEmpty(),
                            TOKEN,
                            MessageType.CUSTOMER,
                            Kind.IMMEDIATE,
                    )
                }
            }
            else
                this.status = TransactionStatus.FAILED
            log.debug("Updated status to ${this.status}")
            this.vasToken = dto.token

            paymentStatus.invoke(this.customerId, this.paymentRefNumber.orEmpty(), dto.status, dto.message, dto.token)

            val updateStatusDTO = PaymentStatusMessageDTO(customerId, this.paymentRefNumber, dto.status,
                this.message, this.vasToken)
            eWalletTransactionClient.updateTransactionStatus(updateStatusDTO)

            log.debug("Payment status published successfully for transactionID: ${dto.transactionId}")
            transactionsRepository.save(this)
        }
    }

    private fun sendSms(phoneNumber: String,
                        countryCode: String,
                        amount: BigDecimal,
                        token: String,
                        units: String,
                        meterNumber: String,
                        applicationContext: String,
                        messageType: MessageType,
                        deliveryKind: Kind? = Kind.IMMEDIATE,
                        delay: BigDecimal? = BigDecimal.ZERO,
                        interval: BigDecimal? = BigDecimal.ZERO): APIResult<String?> {

        val template = notificationProperties.template["token-delivery"]
        val messageBody = MessageFormat.format(template?.get("sms")!!, amount, meterNumber, units, token)
        val identifier = DefaultIdentifier(
            applicationContext,
            phoneNumber,
            messageType
        )
        val notificationOrder = NotificationOrder(
            kind = deliveryKind!!,
            channel = Channels.SMS,
            interval = interval,
            delay = delay,
            message = Message(
                identifier = identifier,
                recipient = phoneNumber,
                countryCode = countryCode,
                subject = LETSHEGO,
                body = messageBody
            )
        )
        log.info("sending ${Channels.SMS} for ${identifier.value()}")
        return notificationClient.notify(notificationOrder)
    }
}