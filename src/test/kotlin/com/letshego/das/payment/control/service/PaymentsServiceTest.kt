package com.letshego.das.payment.control.service

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.config.UtilityProperties
import com.letshego.das.payment.control.mapper.PaymentTransactionMapperImpl
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.control.usecase.*
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.Transactions
import com.letshego.das.payment.entity.dto.PaymentResponseDTO
import com.letshego.das.payment.entity.dto.ProductRequestDTO
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class PaymentsServiceTest {

    @InjectMocks
    private lateinit var paymentsService: PaymentsService

    @Mock
    private lateinit var fundTransferFactory: FundTransferFactory

    @Mock
    private lateinit var fundTransferW2WInternal: FundTransferProviderW2WInternal

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var transactionMapper: PaymentTransactionMapperImpl

    @Mock
    private lateinit var utilityProperties: UtilityProperties

    @Mock
    private lateinit var fundTransferBankToBank: FundTransferBankToBank

    @Mock
    private lateinit var  fundTransferBankToVAS: FundTransferBankToVAS

    @Mock
    private lateinit var  fundTransferBankToWalletExternal: FundTransferBankToWalletExternal

    @Test
    fun `should initiate transaction and return status`() {
        val req = TestUtil.paymentRequestDto()
        val paymentResponseDTO = PaymentResponseDTO("SUCCESS","","","","")
        Mockito.doReturn(fundTransferW2WInternal).`when`(fundTransferFactory).transferProviderFor(req.fromAccountType, req.fromAccountName, req.toAccountType, req.toAccountName, req.toAccountCode, req.transactionAction)
        Mockito.doReturn(PaymentResponseDTO(PaymentStatus.SUCCESS.name, "TR-123", "WK12345", "1")).`when`(fundTransferW2WInternal).transfer(req)
        val result = paymentsService.transferFunds(req)
        assertEquals(paymentResponseDTO.status,result.status)
    }

    @Test
    fun `should return transactionRef`() {
        val req = TestUtil.paymentRequestDto()
        val res = Transactions()
        res.paymentRefNumber = "WK_89008877878887"
        Mockito.doReturn(res).`when`(transactionMapper).toEntity(req)
        Mockito.doReturn(res).`when`(transactionsRepository).save(res)
        val result = paymentsService.getTransactionRef(req)
        assertEquals(res.paymentRefNumber,result)
    }

    @Test
    fun `should return product information`() {
        val map = mapOf(Pair<TransactionAction,String>(TransactionAction.P2M_WICODE,"BK_34587555523"))
        val utilityMap = mapOf(Pair<CountryCodes,Map<TransactionAction,String>>(CountryCodes.namibia,map))
        Mockito.doReturn(utilityMap).`when`(utilityProperties).utility
        val map1 = mapOf(Pair<String,String>("BK_34587555523","BK_34587555523"))
        val map2 = mapOf(Pair<String,Map<String,String>>("BK_34587555523",map1))
        val productMap = mapOf(Pair<CountryCodes,Map<String,Map<String,String>>>(CountryCodes.namibia,map2))
        Mockito.doReturn(productMap).`when`(utilityProperties).productId
        val paymentRequestDto = ProductRequestDTO(CountryCodes.namibia,TransactionAction.P2M_WICODE,"BK_34587555523")
        val result = paymentsService.getProductInformation(paymentRequestDto)
        assertEquals("BK_34587555523",result)
    }

    @Test
    fun `should return bankTransfer`() {
        val req = TestUtil.paymentRequestB2BDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(fundTransferBankToBank).pay(any(),any())
        val result = paymentsService.doBankTransfer(req)
        assertNotNull(result)
    }

    @Test
    fun `should transfer money to vas`() {
        val req = TestUtil.paymentRequestVASDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(fundTransferBankToVAS).pay(any(),any())
        val result = paymentsService.doBankTransfer(req)
        assertNotNull(result)
    }

    @Test
    fun `should return bank2ExterlWalletTransfer`() {
        val req = TestUtil.paymentRequestB2EWDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(fundTransferBankToWalletExternal).pay(any(),any())
        val result = paymentsService.doBankTransfer(req)
        assertNotNull(result)
    }

}