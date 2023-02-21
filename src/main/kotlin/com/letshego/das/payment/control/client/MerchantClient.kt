package com.letshego.das.payment.control.client

import com.letshego.das.payment.entity.dto.PayMerchantAPIRequest
import com.letshego.das.payment.entity.dto.PayMerchantAPIResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "gh-merchant-client", url = "\${service.gh-merchant-client.url}")
interface MerchantClient {

    @PostMapping("/payment")
    fun payMerchant(@RequestBody payMerchant: PayMerchantAPIRequest,
                    @RequestHeader("country") country: String): PayMerchantAPIResponse

}