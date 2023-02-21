package com.letshego.das.payment.control.client

import com.letshego.das.payment.entity.dto.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "ruby-client", url = "\${service.kong-api-gateway.url}")
@RequestMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
interface RubyClient  {

    @RequestMapping(method = [RequestMethod.POST], value = ["/ruby/withdrawals/inter-bank"])
    fun externalBankTransferInitiate(
        request: BankToBankTransferApiRequest
    ): BankToBankApiResponse

    @RequestMapping(method = [RequestMethod.POST], value = ["/ruby/withdrawals/mobile"])
    fun bankToExternalWalletTransferInitiate(
        request: BankToExternalWalletTransferRequestGh
    ): BankToExternalWalletResponseGh

    @RequestMapping(method = [RequestMethod.POST], value = ["/ruby/deposits/mobile"])
    fun initiateExternalWalletTobankTransfer(
        request: BankToExternalWalletTransferRequestGh
    ): BankToExternalWalletResponseGh
}