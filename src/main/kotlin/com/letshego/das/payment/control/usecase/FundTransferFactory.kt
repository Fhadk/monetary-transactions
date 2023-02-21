package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.payment.common.ErrorCodes
import org.springframework.stereotype.Component

@Component
class FundTransferFactory(
    fundTransferB2W: FundTransferProviderB2W,
    fundTransferW2WInternal: FundTransferProviderW2WInternal,
    fundTransferW2WExternal: FundTransferProviderW2WExternal,
    walletVASPayment: WalletVASPayment,
    fundTransferW2BInternal: FundTransferProviderW2BInternal,
    fundTransferW2BExternal: FundTransferProviderW2BExternal,
    fundTransferExternalW2W: FundTransferProviderExternalW2W,
    fundTransferWicode: FundTransferProviderWicode,
    fundTransferExternalP2M: FundTransferProviderExternalP2M,
    fundTransferBuddy: FundTransferProviderBuddy
) {

    private val fundTransferMethodImplementations = mapOf<String, FundTransferProvider>(
            "BANK_ACCOUNT-WALLET" to fundTransferB2W,
            "WALLET-WALLET-INTERNAL" to fundTransferW2WInternal,
            "WALLET-WALLET" to fundTransferW2WExternal,
            "WALLET-VAS_ACCOUNT" to walletVASPayment,
            "WALLET-BANK_ACCOUNT" to fundTransferW2BInternal,
            "WALLET-BANK_ACCOUNT-EXTERNAL" to fundTransferW2BExternal,
            "EXTERNAL-WALLET-WALLET" to fundTransferExternalW2W,
            "WALLET-WICODE" to fundTransferWicode,
            "PAY-2-MERCHANT" to fundTransferExternalP2M,
            "PAY-2-MERCHANT-BUDDY" to fundTransferBuddy
    )

    fun transferProviderFor(fromAccountType: AccountType?, fromAccountName: String?, toAccountType: AccountType?,
                            toAccountName: String?, toAccountCode: String?, transactionAction: TransactionAction?)
            : FundTransferProvider {
        //For all requests with account code, means wallet to bank external!!
        //VERY IMPORTANT
        //This needs to be correctly maintained with F.E. TEAM
        val key = if(isWicodeTransaction(transactionAction))
            "WALLET-WICODE"
        else if(transactionAction == TransactionAction.WALLET_EXTERNAL_P2P)
            "EXTERNAL-WALLET-WALLET"
        else if(isWallet2BankExternal(toAccountCode, toAccountType))
            "WALLET-BANK_ACCOUNT-EXTERNAL"
        else if(isWallet2BankInternal(toAccountCode, toAccountType))
            "WALLET-BANK_ACCOUNT"
        else if (isWalletToWalletInternal(fromAccountType, toAccountType, toAccountCode))
             "WALLET-WALLET-INTERNAL"
        else if (isQRCodeMerchantPayment(toAccountType, transactionAction))
            "PAY-2-MERCHANT"
        else if(isNonQRCodeMerchantPayment(toAccountType, transactionAction))
            "PAY-2-MERCHANT-BUDDY"
        else
            fromAccountType?.name + "-" + toAccountType?.name

        return fundTransferMethodImplementations[key] ?: throw ApplicationException(ErrorCodes.INTERNAL_ERROR)
    }

    private fun isWicodeTransaction(transactionAction: TransactionAction?) =
        transactionAction == TransactionAction.P2M_WICODE

    private fun isWalletToWalletInternal(
        fromAccountType: AccountType?,
        toAccountType: AccountType?,
        toAccountCode: String?
    ) = (fromAccountType == AccountType.WALLET && toAccountType == AccountType.WALLET) && toAccountCode.isNullOrEmpty()

    private fun isWallet2BankExternal(
        toAccountCode: String?,
        toAccountType: AccountType?
    ) = toAccountCode != null && toAccountType == AccountType.BANK_ACCOUNT

    private fun isWallet2BankInternal(
            toAccountCode: String?,
            toAccountType: AccountType?
    ) = toAccountCode == null && toAccountType == AccountType.BANK_ACCOUNT

    private fun isQRCodeMerchantPayment(
        toAccountType: AccountType?,
        transactionAction: TransactionAction?
    ) = toAccountType == AccountType.MERCHANT && transactionAction == TransactionAction.P2M_QR_CODE

    private fun isNonQRCodeMerchantPayment(
        toAccountType: AccountType?,
        transactionAction: TransactionAction?
    ) = toAccountType == AccountType.MERCHANT && transactionAction != TransactionAction.P2M_QR_CODE
}