package com.letshego.das.payment.control.usecase

import com.letshego.das.ms.web.wallet.AccountType
import com.letshego.das.ms.web.wallet.TransactionAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class FundTransferFactoryTest {

    @Mock
    private lateinit var fundTransferB2W: FundTransferProviderB2W

    @Mock
    private lateinit var fundTransferW2WInternal: FundTransferProviderW2WInternal

    @Mock
    private lateinit var fundTransferW2WExternal: FundTransferProviderW2WExternal

    @Mock
    private lateinit var walletVASPayment: WalletVASPayment

    @Mock
    private lateinit var fundTransferW2BInternal: FundTransferProviderW2BInternal

    @Mock
    private lateinit var fundTransferW2BExternal: FundTransferProviderW2BExternal

    @Mock
    private lateinit var fundTransferExternalW2W: FundTransferProviderExternalW2W

    @Mock
    private lateinit var fundTransferWicode: FundTransferProviderWicode

    @Mock
    private lateinit var fundTransferExternalP2M: FundTransferProviderExternalP2M

    @Mock
    private lateinit var fundTransferBuddy: FundTransferProviderBuddy

    @InjectMocks
    private lateinit var fundTransferFactory: FundTransferFactory

    @Test
    fun `should return transfer provider for wiCode`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.BANK_ACCOUNT,"",AccountType.BANK_ACCOUNT,
        "","",TransactionAction.P2M_WICODE)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderWicode")
    }

    @Test
    fun `should return transfer provider for external wallet app`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.BANK_ACCOUNT,"",AccountType.BANK_ACCOUNT,
            "","",TransactionAction.WALLET_EXTERNAL_P2P)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderExternalW2W")
    }

    @Test
    fun `should return transfer provider for wallet to bank external`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.BANK_ACCOUNT,"",AccountType.BANK_ACCOUNT,
            "","AccountCode",null)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderW2BExternal")
    }

    @Test
    fun `should return transfer provider for wallet to bank internal`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.BANK_ACCOUNT,"",AccountType.BANK_ACCOUNT,
            "",null,null)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderW2BInternal")
    }

    @Test
    fun `should return transfer provider for wallet to wallet internal`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.WALLET,"",AccountType.WALLET,
            "",null,null)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderW2WInternal")
    }

    @Test
    fun `should return transfer provider for QR code merchant payment`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.MERCHANT,"",AccountType.MERCHANT,
            "",null,TransactionAction.P2M_QR_CODE)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderExternalP2M")
    }

    @Test
    fun `should return transfer provider for non QR code merchant payment`(){
        val result = fundTransferFactory.transferProviderFor(AccountType.MERCHANT,"",AccountType.MERCHANT,
            "",null,TransactionAction.ADD_MONEY)
        assertEquals(result.javaClass.simpleName,"FundTransferProviderBuddy")
    }

}