package com.letshego.das.payment.control.client

import com.letshego.das.client.kong.PaymentCardRequest
import com.letshego.das.client.kong.PaymentCardResponse
import com.letshego.das.client.wallet.BotswanaBalanceRequest
import com.letshego.das.client.wallet.BotswanaBalanceResponse
import com.letshego.das.ms.CountryCodes
import com.letshego.das.payment.common.*
import com.letshego.das.payment.entity.dto.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "merchant-client", url = "\${service.kong-api-gateway.url}")
interface MerchantTransactionClient {

    @PostMapping("/letshego-api-gw/payments-service")
    fun processWicodeTransaction(@RequestBody wicodePayment: WicodePaymentRequestDTO,
                                 @RequestHeader("x-country") country: String,
                                 @RequestHeader("x-service") service: String = PAYMENT_SERVICE,
                                 @RequestHeader("x-method") method: String = WICODE_TRANSACT): WicodePaymentResponseDTO

    @PostMapping("/letshego-api-gw/payments-service")
    fun processBuddyTransaction(@RequestBody buddyPayment: BuddyPaymentRequestDTO,
                                @RequestHeader("x-country") country: String,
                                @RequestHeader("x-service") service: String = PAYMENT_SERVICE,
                                @RequestHeader("x-method") method: String = PAY_MERCHANT_SERVICE): BuddyPaymentResponseDTO

    @PostMapping("/letshego-api-gw/ewallets-service")
    fun processInternalWalletTransaction(@RequestHeader("x-country") country: String = CountryCodes.botswana.name,
                                         @RequestHeader("x-service") service: String = WALLET_SERVICE,
                                         @RequestHeader("x-method") method: String = SEND_MONEY,
                                         @RequestBody request: W2WTransferInternalClientRequest
    ): WalletTransferInternalResponse

    @PostMapping("/letshego-api-gw/ewallets-service")
    fun getBotswanaBalance(@RequestBody botswanaBalanceRequest: BotswanaBalanceRequest,
                           @RequestHeader("x-country") country: String,
                           @RequestHeader("x-method") method: String,
                           @RequestHeader("x-service") service: String): BotswanaBalanceResponse

    @PostMapping("/letshego-api-gw/cbs-service")
    fun fetchCustomerAccounts(@RequestBody accountInfo: AccountInfoRequest,
                              @RequestHeader("x-country") country: String,
                              @RequestHeader("x-service") service: String,
                              @RequestHeader("x-method") method: String,
                              @RequestHeader("x-user-platform") platform: String): AccountInfoResponse

    @PostMapping("/letshego-api-gw/stanbic-api/transactions/initiate")
    fun paymentCreditDebitCard(@RequestBody paymentCardRequest: PaymentCardRequest): PaymentCardResponse
}