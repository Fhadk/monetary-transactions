package com.letshego.das.payment.boundary

import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.payment.TestUtil
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.control.service.PaymentsService
import com.letshego.das.payment.control.service.SavingsFundsService
import com.letshego.das.payment.control.service.StatusCallBackService
import com.letshego.das.payment.control.usecase.FundTransferProviderExternalW2W
import com.letshego.das.payment.control.usecase.OtpCheck
import com.letshego.das.payment.control.usecase.UpdateTransactionStatus
import com.letshego.das.payment.entity.dto.*
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class PaymentResourceTest {

    @InjectMocks
    private lateinit var paymentResource: PaymentResource

    @Mock
    private lateinit var paymentsService: PaymentsService

    @Mock
    private lateinit var otpCheck: OtpCheck

    @Mock
    private lateinit var updateTransactionStatus: UpdateTransactionStatus

    @Mock
    private lateinit var statusCallBackService: StatusCallBackService

    @Mock
    private lateinit var fundTransferProviderExternalW2W: FundTransferProviderExternalW2W

    @Mock
    private lateinit var transactionsRepository: TransactionsRepository

    @Mock
    private lateinit var savingsFundsService: SavingsFundsService

    @Test
    fun `should initiate transaction and return status`() {
        Mockito.`when`(paymentsService.transferFunds(any()))
                .thenReturn(PaymentResponseDTO("SUCCESS", "tr-1234", "WK1234", "1"))
        val res = paymentResource.makePayment(TestUtil.getPaymentRequest())
        assertNotNull(res)
    }

    @Test
    fun `should return if OTP required`() {
        val req = OtpCheckRequestDTO(CountryCodes.ghana, 1, AccountType.WALLET, "LETSGO", AccountType.BANK_ACCOUNT,
                "CITIBANK", BigDecimal.valueOf(1000))
        Mockito.`when`(otpCheck.invoke(any())).thenReturn(OtpRequiredResDTO(true))
        val res = paymentResource.checkOtpRequired(req)
        assertNotNull(res)
    }

    @Test
    fun `should callback for Transaction Status`() {
        val req = TransactionStatusReqDTO("txn_uei8747474788jdjsd","SUCCESS", BigDecimal.ONE,"token_sdettd66")
        Mockito.doNothing().`when`(updateTransactionStatus).invoke(req)
        val res = paymentResource.transactionStatusCallback(req)
        assertNotNull(res)
    }

    @Test
    fun `should add external funds`() {
        val req = PaymentDTO(67733,CountryCodes.namibia,AccountType.BANK_ACCOUNT,"dsd",
        "7884455","sdfdf",TransactionAction.ADD_MONEY,AccountType.BANK_ACCOUNT,"",
        "","","", BigDecimal.ONE,"","","DEBIT",
        "", ZonedDateTime.now(), BigDecimal.ONE)
        Mockito.doReturn("txn_weredfgdsf").`when`(paymentsService).getTransactionRef(req)
        Mockito.doReturn(PaymentResponseDTO("","","","","")).`when`(fundTransferProviderExternalW2W).pay(req,"txn_weredfgdsf")
        val res = paymentResource.addExternalFunds(req)
        assertNotNull(res)
    }

    @Test
    fun `should return product id`() {
        val req = ProductRequestDTO(CountryCodes.namibia,TransactionAction.ADD_MONEY,"12374848")
        Mockito.doReturn("ProudctId").`when`(paymentsService).getProductInformation(req)
        val res = paymentResource.getProductId(req)
        assertNotNull(res)
    }

    @Test
    fun `should add fund to saving`() {
        val req = TestUtil.getPaymentDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(savingsFundsService).transferToDeposits(req)
        val result = paymentResource.savingsTransferFunds(req)
        assertEquals("SUCCESS", result.data?.status )
    }

    @Test
    fun `should initiate transfer`() {
        val req = TestUtil.getPaymentDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(paymentsService).doBankTransfer(req)
        val result = paymentResource.makeBankAccountPayment(req)
        assertEquals("SUCCESS", result.data?.status )
    }

    @Test
    fun `should initiate transfer to VAS`() {
        val req = TestUtil.getPaymentDto()
        val res = TestUtil.getPaymentResponseDTO()
        Mockito.doReturn(res).`when`(paymentsService).doBankTransfer(req)
        val result = paymentResource.makeBankAccountPayment(req)
        assertEquals("SUCCESS", result.data?.status )
    }

}