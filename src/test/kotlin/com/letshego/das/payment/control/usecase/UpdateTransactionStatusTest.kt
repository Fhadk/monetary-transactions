package com.letshego.das.payment.control.usecase

import com.letshego.das.client.notification.NotificationClient
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.ms.config.NotificationProperties
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.client.EWalletTransactionClient
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.dto.TransactionStatusReqDTO
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class UpdateTransactionStatusTest {

    @InjectMocks
    private lateinit var updateTransactionStatus: UpdateTransactionStatus

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var paymentStatus: PublishPaymentStatus

    @Mock
    private lateinit var notificationClient: NotificationClient

    @Mock
    private lateinit var registrationClient: RegistrationClient

    @Mock
    private lateinit var notificationProperties: NotificationProperties

    @Mock
    private lateinit var eWalletTransactionClient: EWalletTransactionClient

    @Test
    fun `should update transaction to complete if payment status received success`(){
        Mockito.`when`(transactionsRepository.findByPaymentTransactionIdOrPaymentRefNumber(any(), any()))
                .thenReturn(TestUtil.getTransaction())
        updateTransactionStatus.invoke(TransactionStatusReqDTO("TR123", PaymentStatus.SUCCESS.name, BigDecimal.valueOf(1000)))
        Mockito.verify(transactionsRepository, times(1)).save(any())
    }

    @Test
    fun `should update transaction to FAILED if payment status received FAILURE`(){
        Mockito.`when`(transactionsRepository.findByPaymentTransactionIdOrPaymentRefNumber(any(), any()))
                .thenReturn(TestUtil.getTransaction())
        updateTransactionStatus.invoke(TransactionStatusReqDTO("TR123", PaymentStatus.FAILURE.name, BigDecimal.valueOf(1000)))
        Mockito.verify(transactionsRepository, times(1)).save(any())
    }

}