package com.letshego.das.payment.control.client

import com.letshego.das.payment.entity.dto.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "namibia-eft-client", url = "\${service.kong-api-gateway.url}")
@RequestMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
interface NamibiaEFTClient {
    @RequestMapping(method = [RequestMethod.POST], value = ["/eeft/post/rpc"])
    fun bankToExternalBankTransferNamibia(
        request: BankToBankApiRequestNamibiaExternal
    ): BankToExternalWalletResponseNm
}