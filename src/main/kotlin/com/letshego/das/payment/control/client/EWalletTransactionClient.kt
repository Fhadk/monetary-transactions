package com.letshego.das.payment.control.client

import com.letshego.das.ms.web.APIResult
import com.letshego.das.client.wallet.PaymentStatusMessageDTO
import com.letshego.das.client.wallet.TransactionDetailDTO
import com.letshego.das.payment.entity.dto.WalletUpdateBalanceRequestDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "E-Wallet-client", url = "\${service.ewallet-account-management.url}")
interface EWalletTransactionClient {
    @PostMapping(value = ["/api/e-wallet/balance"])
    fun updateBalanceWalletTransaction(@RequestBody dto: WalletUpdateBalanceRequestDTO)

    @PostMapping(value = ["/api/e-wallet/transactions/update-status"])
    fun updateTransactionStatus(@RequestBody dto: PaymentStatusMessageDTO)

    @GetMapping(value=["/api/e-wallet/transactions/details-list/{transactionRef}"])
    fun getWallet(@PathVariable transactionRef: String) : APIResult<List<TransactionDetailDTO?>>
}