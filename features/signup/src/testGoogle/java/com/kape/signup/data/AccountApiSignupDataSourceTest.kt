package com.kape.signup.data

import com.kape.data.model.PurchaseData
import com.kape.signup.domain.Credentials
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.VpnSignUpInformation
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AccountApiSignupDataSourceTest {
    private val api = mockk<AndroidAccountAPI>()
    private val dataSource = AccountApiSignupDataSource(api)
    private val purchase = PurchaseData(token = "token", productId = "yearly", orderId = "order")

    private fun respondToSignup(
        details: VpnSignUpInformation?,
        errors: List<AccountRequestError> = emptyList(),
    ) {
        every { api.vpnSignUp(any(), captureLambda()) } answers {
            lambda<(VpnSignUpInformation?, List<AccountRequestError>) -> Unit>().invoke(details, errors)
        }
    }

    @Test
    fun `sends the receipt and device id, and returns the new credentials`() =
        runTest {
            respondToSignup(VpnSignUpInformation("active", "p1234567", "secret"))

            assertEquals(
                AccountCreation.Created(Credentials("active", "p1234567", "secret")),
                dataSource.createAccount(purchase, "device-id"),
            )
            verify {
                api.vpnSignUp(
                    match {
                        it.obfuscatedDeviceId == "device-id" &&
                            it.receipt.orderId == "order" &&
                            it.receipt.token == "token" &&
                            it.receipt.sku == "yearly"
                    },
                    any(),
                )
            }
        }

    @Test
    fun `reports a connectivity failure as a network error`() =
        runTest {
            respondToSignup(null, listOf(AccountRequestError(601, "offline", 0)))

            assertEquals(AccountCreation.NetworkError("offline"), dataSource.createAccount(purchase, "device-id"))
        }

    @Test
    fun `reports a server error as rejected`() =
        runTest {
            respondToSignup(null, listOf(AccountRequestError(400, "bad receipt", 0)))

            assertEquals(AccountCreation.Rejected, dataSource.createAccount(purchase, "device-id"))
        }

    @Test
    fun `setting the email succeeds only without errors`() =
        runTest {
            every { api.setEmail("a@b.c", false, captureLambda()) } answers {
                lambda<(String?, List<AccountRequestError>) -> Unit>().invoke(null, emptyList())
            }
            assertTrue(dataSource.setEmail("a@b.c"))

            every { api.setEmail("a@b.c", false, captureLambda()) } answers {
                lambda<(String?, List<AccountRequestError>) -> Unit>().invoke(null, listOf(AccountRequestError(500, "boom", 0)))
            }
            assertFalse(dataSource.setEmail("a@b.c"))
        }
}