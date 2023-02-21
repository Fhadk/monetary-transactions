package com.letshego.das.payment.control.service

import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.common.ErrorCodes
import com.letshego.das.payment.control.usecase.FundTransferProviderBankInternal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class SavingsFundsServiceTest {

    @Mock
    private lateinit var fundsTransferProviderBankInternal: FundTransferProviderBankInternal

    @InjectMocks
    private lateinit var savingsFundsService: SavingsFundsService

    @Test
    fun `should transfer to deposit`() {
        val req = TestUtil.paymentRequestDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(fundsTransferProviderBankInternal).transfer(req)
        val result = savingsFundsService.transferToDeposits(req)
        assertNotNull(result)
    }
}