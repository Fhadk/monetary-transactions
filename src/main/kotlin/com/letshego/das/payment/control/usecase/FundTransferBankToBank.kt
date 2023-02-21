package com.letshego.das.payment.control.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.letshego.das.ms.CountryCodes
import com.letshego.das.ms.EMPTY_STRING
import com.letshego.das.ms.config.CountryProperties
import com.letshego.das.ms.exception.ApplicationException
import com.letshego.das.payment.common.*
import com.letshego.das.payment.control.client.*
import com.letshego.das.payment.control.mapper.PaymentTransactionMapper
import com.letshego.das.payment.control.repository.TransactionsRepository
import com.letshego.das.payment.entity.domain.PaymentStatus
import com.letshego.das.payment.entity.domain.ResponseCode
import com.letshego.das.payment.entity.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.streams.toList

@Component
class FundTransferBankToBank(
    transactionMapper: PaymentTransactionMapper,
    transactionsRepository: TransactionsRepository,
    private val rubyClient: RubyClient,
    private val cbsClient: CBSClient,
    private val kongClient: com.letshego.das.client.wallet.KongClient,
    private val eWalletTransactionClient: EWalletTransactionClient,
    private val countryProperties: CountryProperties,
    private val merchantTransactionClient: MerchantTransactionClient,
    private val namibiaEFTClient: NamibiaEFTClient,
    private val updateTransactionStatus: UpdateTransactionStatus
) : FundTransferProviderBase(
    kongClient,
    eWalletTransactionClient,
    transactionsRepository,
    transactionMapper,
    merchantTransactionClient,
    namibiaEFTClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun pay(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {

        if(request.toAccountRef == request.fromAccountRef)
            throw ApplicationException(ErrorCodes.BAD_REQUEST, "Cannot send to own account.")

        return when (request.country) {

            CountryCodes.ghana -> {
                if(request.toAccountCode.isNullOrBlank())
                    initiateInternalBankToBankTransfer(request, transactionRef)
                else
                    initiateBankToBankTransferForGhana(request, transactionRef)
            }

            CountryCodes.namibia -> {
                if (request.toAccountCode.isNullOrEmpty())
                    initiateInternalBankToBankTransfer(request, transactionRef)
                else
                    initiateBankToBankExtTransferForNamibia(request, transactionRef)
            }

            else ->
                throw ApplicationException(ErrorCodes.COUNTRY_NOT_SUPPORTED)
        }
    }

    private fun initiateBankToBankTransferForGhana(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status: String
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR
        try {
            log.info("Executing transfer-to-deposit request from kong gateway: $request")
            val response = rubyClient.externalBankTransferInitiate(
                toBankToBankTransferApiRequestForGhana(request, transactionRef)
            )

            checkError(response)

            log.info("Transaction has been processed successfully from KONG")
            status = PaymentStatus.SUCCESS.name
            transactionId = transactionRef
            //message = objectMapper.writeValueAsString(response.data!!.okMessage)
            responseCode = ResponseCode.SUCCESS

        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        this.addBankTransaction(request, status, message.orEmpty(), transactionRef)
        return PaymentResponseDTO(
            status,
            transactionRef,
            transactionRef,
            responseCode.code.toString(),
            message
        )
    }

    private fun toBankToBankTransferApiRequestForGhana(
        request: PaymentDTO,
        transactionRef: String
    ): BankToBankTransferApiRequest {
        return BankToBankTransferApiRequest(
            request.amount,
            countryProperties.config[request.country]?.currencyCode.toString(),
            CountryCodes.ghana.name,
            request.fromAccountRef.orEmpty(),
            request.toAccountCode.orEmpty(),
            request.toAccountName.orEmpty(),
            request.toAccountRef.orEmpty(),
            transactionRef
        )
    }

    private fun toBankToBankTransferApiRequestForNamibia(
        request: PaymentDTO,
        transactionRef: String
    ): BankToBankApiRequest {
        return BankToBankApiRequest(
            BankToBankApiRequest.PBTransferToDepositAccount(
                BankToBankApiRequest.XferToDepAcctRq(
                    BankToBankApiRequest.RqHeader("WAL", "", "0001"),
                    BankToBankApiRequest.RequestData(
                        request.amount.toString(),
                        request.amount.toPlainString(),
                        countryProperties.config[request.country]?.currencyCode.toString(),
                        request.fromAccountRef!!,
                        if(isSendingToOwnAccount(request.toAccountRef.orEmpty(), request.fromAccountRef, request.country)) "" else "IA",
                        request.toAccountRef!!,
                        request.beneficiaryReference ?: "Bank transfer"
                    )
                )
            )
        )
    }

    private fun checkError(response: BankToBankApiResponse) {
        if (response.status.equals("fail", true)) {
            throw ApplicationException(
                ErrorCodes.KONG_GATEWAY_ERROR,
                response.message
            )
        }
    }

    private fun checkErrorForNamibia(response: InternalBankToBankApiResponse) {
        if (response.nPBTransferToDepositAccountResponse.xferToDepAcctRs.stat!!.errorMessage != null) {
            throw ApplicationException(
                ErrorCodes.KONG_GATEWAY_ERROR,
                response.nPBTransferToDepositAccountResponse.xferToDepAcctRs.stat!!.errorMessage!!.errMsg
            )
        }
    }

    private fun initiateInternalBankToBankTransfer(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status: String
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR
        try {
            log.info("Executing transfer-to-deposit request from kong gateway: $request")
            val response =  cbsClient.bankTransferInitiate(
                request.country?.name ?: throw ApplicationException(ErrorCodes.BAD_REQUEST, "No country provided"),
                toBankToBankTransferApiRequestForNamibia(request, transactionRef)
            )

            checkErrorForNamibia(response)

            log.info("Transaction has been processed successfully from KONG")
            status = PaymentStatus.SUCCESS.name
            transactionId = transactionRef
            responseCode = ResponseCode.SUCCESS

        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        this.addBankTransaction(request, status, message.orEmpty(), transactionId.orEmpty())
        updateTransactionStatus(TransactionStatusReqDTO(transactionId.orEmpty(),status,request.amount,"Transaction updated Successfully"))
        return PaymentResponseDTO(
            status,
            transactionRef,
            transactionRef,
            responseCode.code.toString(),
            message
        )
    }


    private fun initiateBankToBankExtTransferForNamibia(request: PaymentDTO, transactionRef: String): PaymentResponseDTO {
        var status: String
        var transactionId: String? = null
        var message: String? = null
        var responseCode = ResponseCode.ERROR
        try {
            log.info("Executing transfer-to-deposit request from EEFT gateway: $request")
          val  objectMapper = ObjectMapper()
            objectMapper.writeValueAsString( toExtBankNam(request, transactionRef))
            val response =  namibiaEFTClient.bankToExternalBankTransferNamibia(
                toExtBankNam(request, transactionRef)
            )

            log.info("Transaction has been processed successfully from EEFT")
            status = PaymentStatus.SUCCESS.name
            transactionId = transactionRef
            responseCode = ResponseCode.SUCCESS

        } catch (ex: Exception) {
            log.error("Payment for transactionRef: $transactionRef, FAILED", ex)
            status = PaymentStatus.FAILURE.name
            message = ex.message
        }
        this.addBankTransaction(request, status, message.orEmpty(), transactionId.orEmpty())
        return PaymentResponseDTO(
            status,
            transactionRef,
            transactionRef,
            responseCode.code.toString(),
            message
        )
    }

    private fun toExtBankNam(dto: PaymentDTO, transactionRef: String): BankToBankApiRequestNamibiaExternal {
        return BankToBankApiRequestNamibiaExternal(
            "2", JSON_RPC, METHOD,
            Param(
                transactionRef,
                UUIDSOURCE,
                dto.fromAccountRef.orEmpty(),
                "External Funds trf",
                dto.amount.toString(),
                CURRENCY_NAD,
                dto.toAccountName.orEmpty(),
                dto.message,
                EMPTY_STRING,
                EMPTY_STRING,
                dto.amount.toString(),
                CURRENCY_NAD,
                dto.toAccountRef.orEmpty(),
                EMPTY_STRING,
                EMPTY_STRING,
                dto.toAccountCode.orEmpty(),
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                ENTRY_CLASS_CODE,
                SERVICE_TYPE,
                transactionRef
            )
        )

    }

    private fun isSendingToOwnAccount(toAccountRef: String, fromAccountRef: String, country: CountryCodes?): Boolean{

        val response = merchantTransactionClient.fetchCustomerAccounts(AccountInfoRequest(fromAccountRef),
        country?.name.orEmpty(), CBS_SERVICE, ACCOUNT_INFO, PLATFORM)

        val filterAccounts = response.data.accounts.stream().filter{
            x -> x.accountNumber == toAccountRef
        }.toList()

        if(filterAccounts.isEmpty())
            return false
        return true;
    }
}