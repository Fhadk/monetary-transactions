package com.letshego.das.payment.control.client

import com.letshego.das.payment.entity.dto.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "cbs-client", url = "\${service.kong-api-gateway.url}")
@RequestMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
interface CBSClient {

    @RequestMapping(method = [RequestMethod.POST], value = ["/letshego-api-gw/cbs-service"])
    fun cbsCall(
        @RequestHeader("x-country") country: String,
        @RequestHeader("x-service") service: String,
        @RequestHeader("x-method") method: String,
        request: CBSTransferDepositGatewayRequest
    ): CBSGatewayResponse<CBSTransferDepositGatewayResponse>

    @RequestMapping(method = [RequestMethod.POST], value = ["/account/deposit"])
    fun bankTransferInitiate(
        @RequestHeader("country") country: String,
        request: BankToBankApiRequest
    ): InternalBankToBankApiResponse

    @RequestMapping(method = [RequestMethod.POST], value = ["/letshego-api-gw/mobipay-vas-api/1.0/e-wallet"])
    fun walletTransferInitiate(
        request: BankToExternalWalletRequestNm
    ): BankToExternalWalletResponseNm

    @RequestMapping(method = [RequestMethod.POST], value = ["/letshego-api-gw/mobipay-vas-api/1.0/buy-electricity"])
    fun namibiaElectricityPayment(
        request: BankToVASPaymentNm
    ): BankToExternalWalletResponseNm

    @RequestMapping(method = [RequestMethod.POST], value = ["/letshego-api-gw/mobipay-vas-api/1.0/net-payment"])
    fun namibiaVASPayment(
        request: BankToVASPaymentNm
    ): BankToExternalWalletResponseNm
}