package com.letshego.das.payment

import com.letshego.das.client.registration.CustomerResponse
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.client.vas.HubtleBillPayResponse
import com.letshego.das.client.wallet.BalanceAPIResponse
import com.letshego.das.client.wallet.BalanceData
import com.letshego.das.client.wallet.BalanceDataResponse
import com.letshego.das.client.wallet.TransactionDetailDTO
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.web.APIResult
import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionAction
import com.letshego.das.ms.web.wallet.TransactionStatus
import com.letshego.das.ms.web.wallet.TransactionType
import com.letshego.das.payment.common.PAYMENT_SERVICE
import com.letshego.das.payment.config.PaymentProperties
import com.letshego.das.payment.entity.domain.Transactions
import com.letshego.das.payment.entity.dto.*
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

object TestUtil {
    private lateinit var registrationClient: RegistrationClient

    fun getPaymentProperties(): PaymentProperties {
        val props = PaymentProperties()

        val ghanaLimit = PaymentProperties.TransactionLimit()
        ghanaLimit.limit = mutableMapOf()
        ghanaLimit.limit["bank_account-wallet"] = BigDecimal.valueOf(1000)
        ghanaLimit.limit["wallet-bank_account"] = BigDecimal.valueOf(1000)


        val ghanaConfig = PaymentProperties.PaymentConfig()
        ghanaConfig.currencyCode = "GHD"
        ghanaConfig.transaction = ghanaLimit
        ghanaConfig.monthly = ghanaLimit


        props.config = mapOf(CountryCodes.ghana to ghanaConfig)
        return props
    }

    fun getTransaction(amount: BigDecimal = BigDecimal.TEN) = Transactions(amount = amount, paymentRefNumber = "2331")

    fun getPaymentRequest(
        fromAccountType: AccountType = AccountType.WALLET,
        toAccountType: AccountType = AccountType.WALLET
    ) =
        PaymentDTO(
            1, CountryCodes.ghana, fromAccountType, "LETSHEGO", "1234567",
            null, TransactionAction.VAS_INTERNET, toAccountType, "Surfline (4G)", "843632874", "857",
            "TR12345", BigDecimal.valueOf(1000), "Sample trans")


    fun getCustomer() = APIResult?.success("Customer found", sampleCustomerResponse())

    fun sampleCustomerResponse(): com.letshego.das.client.registration.CustomerResponse {
        return CustomerResponse(
            "+264",
        "23324234",
        "abc@xyz.com",
        "Oaitse",
        "Mitchell",
        "223",
        "das",
        12345,
        "",
        "",
        "",
        true,
        true,
        )
    }

    fun getPaymentDto() =
        PaymentDTO(
            customerId = 1,
            country = CountryCodes.botswana,
            fromAccountType = null,
            fromAccountName = null,

            fromAccountRef = null,
            fromAccountCode = null,
            transactionAction = null,
            toAccountType = null,

            toAccountName = null,
            toAccountRef = null,
            toAccountCode = null,

            transactionRef = "12313",
            amount = BigDecimal.ZERO,
            message = null,
            email = null,
            wicodeTransactionType = null,
            qrCode = null,
            transactionTime = null,
            balance = BigDecimal.ZERO,
            currencyCode = null
        )

    fun getCBSGatewayResponse() = CBSGatewayResponse(
        null,
        CBSGatewayResponse.Data(
            CBSTransferDepositGatewayResponse(
                "413889", "O.K.  001", "O.K.  001413889", "0000", "413889"
            )
        )
    )
    fun getTransactionDetailDTOResponse() = APIResult?.success("Customer found", sampleTransactionDetailDTO())

    fun sampleTransactionDetailDTO(): List<TransactionDetailDTO> {
        return listOf(TransactionDetailDTO(89765,"DEBIT","ADD_MONEY","WK346677",
            ZonedDateTime.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,TransactionStatus.COMPLETE,"tesgeed","788234234",
        AccountType.BANK_ACCOUNT,"Test Bank","898777","7887sdd",null,false,
        "vasToken",null,null,null,false, BigDecimal.ZERO),
            TransactionDetailDTO(89765,"CREDIT","ADD_MONEY","WK346677",
                ZonedDateTime.now(), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,TransactionStatus.COMPLETE,"tesgeed","788234234",
                AccountType.BANK_ACCOUNT,"90876654565","89877798","7887988",null,false,
                "vasToken",null,null,null,false, BigDecimal.ZERO))
    }

    fun getWalletBalance() = APIResult?.success("Customer found", sampleWalletBalance())

    fun sampleWalletBalance(): BalanceAPIResponse {
       return BalanceAPIResponse(BalanceDataResponse(listOf(BalanceData(null,null,"EMONEY_1",
       "","10","","10","")),"trans_775858"),"200","")
    }

    fun paymentRequestDto(): PaymentDTO{
        return PaymentDTO(67733, CountryCodes.ghana,AccountType.BANK_ACCOUNT,"dsd",
            "7884455","sdfdf", TransactionAction.VAS_DATA_BUNDLE,AccountType.BANK_ACCOUNT,"",
            "","","", BigDecimal.ONE,"","","DEBIT",
            "", ZonedDateTime.now(), BigDecimal.ONE)
    }

    fun paymentRequestVASDto(): PaymentDTO{
        return PaymentDTO(67733, CountryCodes.ghana,AccountType.BANK_ACCOUNT,"dsd",
            "7884455","sdfdf", TransactionAction.VAS_DATA_BUNDLE,AccountType.VAS_ACCOUNT,"",
            "","","", BigDecimal.ONE,"","","DEBIT",
            "", ZonedDateTime.now(), BigDecimal.ONE)
    }

    fun getPaymentResponseDTO(): PaymentResponseDTO {
        return PaymentResponseDTO("SUCCESS","txn_ei8874747475","WK_789ddd988",
        "rscode","")
    }

    fun getMobifinResponse(): MobifinResponse {
        return MobifinResponse("1","SUCCESS", MobifinResponseData("txn_ei8874747475"))
    }

    fun getMobifinResponseForFailure(): MobifinResponse {
        return MobifinResponse("200","SUCCESS", MobifinResponseData("txn_ei8874747475"))
    }

    fun getCBSGatewayResponseForFailure() = CBSGatewayResponse(
        CBSGatewayResponse.Error("","Test Failure","400"),
        CBSGatewayResponse.Data(
            CBSTransferDepositGatewayResponse(
                "413889", "O.K.  001", "O.K.  001413889", "0000", "413889"
            )
        )
    )

    fun getBuddyPaymentResponseDTO(): BuddyPaymentResponseDTO {
        return BuddyPaymentResponseDTO("SUCCESS","Test",200, BuddyPaymentRequestDTO(
            "txn_ref34566444",TransactionType.DEBIT.name, BigDecimal.ONE,"usd","77755",
            true, BuddyCustomerData(
                BuddyCustomerLocation("997474","8488448"),"test name",
            "msisdn"
            ), MerchantPaymentData("accou89988","push")
        ),"cbstransid","cbsError" )
    }

    fun getBuddyPaymentResponseDTOForFailure(): BuddyPaymentResponseDTO {
        return BuddyPaymentResponseDTO("SUCCESS","Test",201, BuddyPaymentRequestDTO(
            "txn_ref34566444",TransactionType.DEBIT.name, BigDecimal.ONE,"usd","77755",
            true,BuddyCustomerData(BuddyCustomerLocation("997474","8488448"),"test name",
                "msisdn"),MerchantPaymentData("accou89988","push")
        ),"cbstransid","cbsError" )
    }

    fun getPayMerchantAPIResponse(): PayMerchantAPIResponse {
        return PayMerchantAPIResponse(listOf(Response("SUCCESS","TEST")),87987777,null)
    }

    fun getPayMerchantAPIResponseForFailure(): PayMerchantAPIResponse {
        return PayMerchantAPIResponse(listOf(Response("FAILURE","TEST")),87987777,null)
    }

    fun getBankToBankApiResponseForNamibia(): InternalBankToBankApiResponse {
        return InternalBankToBankApiResponse(InternalBankToBankApiResponse.PBTransferToDepositAccountResponse(
            InternalBankToBankApiResponse.XferToDepAcctRs(
                InternalBankToBankApiResponse.RsHeader("test","success","output string"),
                InternalBankToBankApiResponse.ResponseData(
                    InternalBankToBankApiResponse.OkMessageResponse("filter1","filter2","reqData","custNum"),
                    null//BankToBankApiResponseForNamibia.ErrorMessageResponse("error","345325","401")
                )
            )
        ))
    }

    fun getBankToBankApiResponseForNamibiaForFailure(): InternalBankToBankApiResponse {
        return InternalBankToBankApiResponse(InternalBankToBankApiResponse.PBTransferToDepositAccountResponse(
            InternalBankToBankApiResponse.XferToDepAcctRs(
                InternalBankToBankApiResponse.RsHeader("test","success","output string"),
                InternalBankToBankApiResponse.ResponseData(
                    InternalBankToBankApiResponse.OkMessageResponse("filter1","filter2","reqData","custNum"),
                    InternalBankToBankApiResponse.ErrorMessageResponse("error","345325","401")
                )
            )
        ))
    }

    fun paymentRequestDtoForGhana(): PaymentDTO{
        return PaymentDTO(67733, CountryCodes.ghana,AccountType.BANK_ACCOUNT,"dsd",
            "7884455","sdfdf", TransactionAction.P2M_WICODE,AccountType.BANK_ACCOUNT,"",
            "","","", BigDecimal.ONE,"","","DEBIT",
            "", ZonedDateTime.now(), BigDecimal.ONE)
    }

    fun getBankToBankApiResponse(): BankToBankApiResponse {
        return BankToBankApiResponse("success", BankToBankApiResponse.BankToBankApiData(
            "uuid",
            "success",
            "trxn_code",
            "provider_txid",
            "extrnx_code",
            "walletno",
            BigDecimal.ONE,
            "p",
            "cstm",
            false,
            "cbs_txid",
            "",
            BankToBankApiResponse.CBSData(
                "uuid",
                "success",
                "trxn_code",
                "provider_txid"
            )
        ),"message","402")
    }

    fun getBankToBankApiResponseForFailure(): BankToBankApiResponse {
        return BankToBankApiResponse("fail", BankToBankApiResponse.BankToBankApiData(
            "uuid",
            "success",
            "trxn_code",
            "provider_txid",
            "extrnx_code",
            "walletno",
            BigDecimal.ONE,
            "p",
            "cstm",
            false,
            "cbs_txid",
            "",
            BankToBankApiResponse.CBSData(
                "uuid",
                "success",
                "trxn_code",
                "provider_txid"
            )
        ),"message","402")
    }

    fun getWicodePaymentResponseDTO(): WicodePaymentResponseDTO {
        return WicodePaymentResponseDTO("SUCCESS","",200, WicodePaymentResponseDataDTO(
            "esbReferrence",
            BigDecimal.ONE,
            "wicode",
            "account number"
        )
        )
    }

    fun getWicodePaymentResponseDTOForFailure(): WicodePaymentResponseDTO {
        return WicodePaymentResponseDTO("SUCCESS","",201, WicodePaymentResponseDataDTO(
            "esbReferrence",
            BigDecimal.ONE,
            "wicode",
            "account number"
        )
        )
    }

    fun paymentRequestDtoForWicode(): PaymentDTO{
        return PaymentDTO(67733, CountryCodes.ghana,AccountType.BANK_ACCOUNT,"dsd",
            "7884455","sdfdf", TransactionAction.P2M_WICODE,AccountType.BANK_ACCOUNT,"",
            "","","", BigDecimal.ONE,"","", PAYMENT_SERVICE,
            "", ZonedDateTime.now(), BigDecimal.ONE)
    }

    fun getPaymentResponseDTOForFailure(): PaymentResponseDTO {
        return PaymentResponseDTO("FAILURE","txn_ei8874747475","WK_789ddd988",
            "rscode","")
    }

    fun getBankToExternalWalletResponseNm(): BankToExternalWalletResponseNm {
        return BankToExternalWalletResponseNm(
            DataNm(
                UUID.randomUUID().toString(),
                "",
                BigDecimal.ONE.toString(),
                "MTN",
                "NAD","",
            ),
            "Success"
        )

    }

    fun getBankToExternalWalletResponseGh(): BankToExternalWalletResponseGh {
        return BankToExternalWalletResponseGh(
            DataGh(
                BigDecimal.ONE,
                CbsDataGh(
                    "12345678",
                    "ewallet",
                    "ghana"
                ),
                "",
                "tnx_12345",
                CustomData(),
                "ext_1234",
                true,
                "MTN",
                "provider_12345",
                "cbs_pending",
                "WTX00002406",
                UUID.randomUUID().toString(),
                "0243149015",
            ),
            "success"
        )
    }


    fun getHubtleBillPayResponse(): HubtleBillPayResponse {
        return HubtleBillPayResponse("SUCCESS","","externalReference", HubtleBillPayResponse.Detail(
            "", BigDecimal.ONE, "utilityType", "provider", true, HubtleBillPayResponse.CBSResponseData(
                "SUCCESS", "uuidNum", "cbs_txid", "cbsCode", "cbsMsg", "accountNumber",
                "ghana"
            ), HubtleBillPayResponse.Vendor("", "", HubtleBillPayResponse.Data("", BigDecimal.ZERO, "trxid",
                HubtleBillPayResponse.Meta("")
            ))
        )
        )
    }

    fun getHubtleBillPayResponseForFailure(): HubtleBillPayResponse {
        return HubtleBillPayResponse("SUCCESS","","externalReference", HubtleBillPayResponse.Detail(
            "", BigDecimal.ONE, "utilityType", "provider", true, HubtleBillPayResponse.CBSResponseData(
                "FAILURE", "uuidNum", "cbs_txid", "cbsCode", "cbsMsg", "accountNumber",
                "ghana"
            ), HubtleBillPayResponse.Vendor("", "", HubtleBillPayResponse.Data("", BigDecimal.ZERO, "trxid",
                HubtleBillPayResponse.Meta("")
            ))
        )
        )
    }

    fun paymentRequestB2BDto(): PaymentDTO{
        return PaymentDTO(67733, CountryCodes.ghana,AccountType.BANK_ACCOUNT,"dsd",
            "7884455","sdfdf", TransactionAction.BANK_P2P_TRANSFER,AccountType.BANK_ACCOUNT,"",
            "","","", BigDecimal.ONE,"","","DEBIT",
            "", ZonedDateTime.now(), BigDecimal.ONE)
    }

    fun paymentRequestB2EWDto(): PaymentDTO{
        return PaymentDTO(67733, CountryCodes.ghana,AccountType.BANK_ACCOUNT,"dsd",
            "7884455","sdfdf", TransactionAction.BANK_TO_EXTERNAL_WALLET,AccountType.WALLET,"",
            "","","", BigDecimal.ONE,"","","DEBIT",
            "", ZonedDateTime.now(), BigDecimal.ONE)
    }
}

