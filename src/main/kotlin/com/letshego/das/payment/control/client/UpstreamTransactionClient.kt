package com.letshego.das.payment.control.client

import com.letshego.das.payment.entity.dto.WalletToBankAPIRequest
import com.letshego.das.payment.entity.dto.MobifinResponse
import com.letshego.das.payment.entity.dto.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "kong-client-monetary", url = "\${service.kong-api-gateway.url}")
interface UpstreamTransactionClient {

    @PostMapping(value = ["/mfselite/sendMoney"])
    fun initiateW2WInternalTransfer(
            @RequestHeader("country") country: String,
            @RequestBody requestInternal: W2WTransferInternalClientRequest
    ): WalletTransferInternalResponse

    @PostMapping(value = ["/mfselite/sendMoneyExternal"])
    fun initiateW2WExternalTransfer(
            @RequestHeader("country") country: String,
            @RequestBody requestExternal: W2WTransferExternalClientRequest
    ): WalletTransferInternalResponse

    @PostMapping(value = ["/mfselite/banktowallet"])
    fun initiateB2WInternalTransfer(
            @RequestHeader("country") country: String,
            request: BankToWalletInternalAPIRequest
    ): MobifinResponse

    @PostMapping(value = ["/mfselite/topup"])
    fun initiateVasPayment(
            @RequestHeader("country") country: String,
            @RequestBody requestExternal: VASPaymentClientRequestDTO
    ): WalletTransferInternalResponse

    @PostMapping("/mfselite/wallettobank")
    fun initiateWalletToBankTransfer(
            @RequestHeader("country") country: String,
            @RequestBody bankToWalletAPIRequest: WalletToBankAPIRequest
    ): MobifinResponse

    @PostMapping("/mfselite/walletToExternalBank")
    fun initiateWalletToBankExternalTransfer(
            @RequestHeader("country") country: String,
            @RequestBody bankToWalletAPIRequest: WalletToBankExternalAPIRequest
    ): MobifinResponse

    @PostMapping("/mfselite/getMoneyExternal")
    fun initiateExternalWalletToWalletTransfer(
            @RequestHeader("country") country: String,
            @RequestBody walletToWalletRequest: ExternalW2WTransferClientRequest
    ): MobifinResponse
}