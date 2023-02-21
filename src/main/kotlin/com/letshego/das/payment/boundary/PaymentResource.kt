package com.letshego.das.payment.boundary

import com.letshego.das.ms.ID_TOKEN_CUSTOMER_ID
import com.letshego.das.ms.context.Authorized
import com.letshego.das.ms.web.APIResult
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.control.service.PaymentsService
import com.letshego.das.payment.control.service.SavingsFundsService
import com.letshego.das.payment.control.service.StatusCallBackService
import com.letshego.das.payment.control.usecase.FundTransferProviderExternalW2W
import com.letshego.das.payment.control.usecase.OtpCheck
import com.letshego.das.payment.control.usecase.UpdateTransactionStatus
import com.letshego.das.payment.entity.dto.*
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.validation.Valid

@PaymentApi
@Validated
class PaymentResource(
    private val paymentsService: PaymentsService,
    private val otpCheck: OtpCheck,
    private val updateTransactionStatus: UpdateTransactionStatus,
    private val statusCallBack: StatusCallBackService,
    val fundTransferProviderExternalW2W: FundTransferProviderExternalW2W,
    val transactionsRepository: TransactionsRepository,
    private val savingsFundsService: SavingsFundsService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/transaction/otp-check")
    fun checkOtpRequired(@RequestBody @Valid dto: OtpCheckRequestDTO): APIResult<OtpRequiredResDTO> {
        log.info("Check OTP required for transaction request received.: $dto")
        return APIResult.success("OTP check done", otpCheck(dto))
    }

    //@Authorized(ID_TOKEN_CUSTOMER_ID, "dtoPayment.customerId")
    @PostMapping("/funds/transfer/initiate")
    fun makePayment(@RequestBody @Valid dtoPayment: PaymentDTO): APIResult<PaymentResponseDTO> {
        log.info("Wallet to wallet internal transfer initiated for req: $dtoPayment")
        return APIResult.success("Payment complete", paymentsService.transferFunds(dtoPayment))
    }

    @PutMapping("/transaction/status/callback")
    fun transactionStatusCallback(@RequestBody dto: TransactionStatusReqDTO) =
            APIResult.success("Status updated successfully.", updateTransactionStatus(dto))

    @PostMapping("/external/funds")
    fun addExternalFunds(@RequestBody @Valid dtoPayment: PaymentDTO): APIResult<PaymentResponseDTO> {
        log.info("External Wallet to wallet internal transfer initiated for req: $dtoPayment")
        return APIResult.success("Payment complete", fundTransferProviderExternalW2W.pay(
            dtoPayment, paymentsService.getTransactionRef(dtoPayment)
        ))
    }

    @PostMapping("/vas/productId")
    fun getProductId(@RequestBody product: ProductRequestDTO): APIResult<String>{
        return APIResult.success("Successfully obtained to product id",
            paymentsService.getProductInformation(product)
        )
    }

    @PostMapping("/savings/funds/transfer")
    fun savingsTransferFunds(@RequestBody dtoPayment: PaymentDTO): APIResult<PaymentResponseDTO> {
        return APIResult.success("Transfer to accounts", savingsFundsService.transferToDeposits(dtoPayment))
    }

    //@Authorized(ID_TOKEN_CUSTOMER_ID, "payment.customerId")
    @PostMapping("/bank-account/transfer/initiate")
    fun makeBankAccountPayment(@RequestBody @Valid payment: PaymentDTO): APIResult<PaymentResponseDTO>{
        return APIResult.success("Transaction successfully", paymentsService.doBankTransfer(payment))
    }

    @PostMapping("/gh-wallet/transaction/status/callback")
    fun ghWalletStatusCallback(@RequestBody request: WalletStatusRequest): WalletStatusResponse{
        return statusCallBack(request)
    }
}