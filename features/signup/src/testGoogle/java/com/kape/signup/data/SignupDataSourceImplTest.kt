package com.kape.signup.data

import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.signup.data.models.Credentials
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.VpnSignUpInformation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals

internal class SignupDataSourceImplTest {
    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private val eventGenerator: KpiEventGenerator = mockk(relaxed = true)
    private val submitKpiEventUseCase: SubmitKpiEventUseCase = mockk(relaxed = true)

    private lateinit var source: SignupDataSourceImpl

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {}
        source = SignupDataSourceImpl(api, eventGenerator, submitKpiEventUseCase)
    }

    @Test
    fun `signup success`() =
        runTest {
            val expected = Credentials("ok", "username", "password")
            val signupInfo =
                VpnSignUpInformation(expected.status, expected.username, expected.password)
            coEvery { api.vpnSignUp(any(), any()) } answers {
                lastArg<(VpnSignUpInformation?, List<AccountRequestError>) -> Unit>().invoke(
                    signupInfo,
                    emptyList(),
                )
            }
            val actual = source.vpnSignup("orderId", "token", "productId", "obfuscatedDeviceId")
            assertEquals(expected, actual)
        }

    @Test
    fun `signup fails on authoritative server rejection without retrying`() =
        runTest {
            coEvery { api.vpnSignUp(any(), any()) } answers {
                lastArg<(VpnSignUpInformation?, List<AccountRequestError>) -> Unit>().invoke(
                    null,
                    listOf(AccountRequestError(400, "Invalid receipt")),
                )
            }
            val actual = source.vpnSignup("orderId", "token", "productId", "obfuscatedDeviceId")
            assertNull(actual)
            verify(exactly = 1) { api.vpnSignUp(any(), any()) }
        }

    @Test
    fun `vpnSignup retries network failure 3 times`() =
        runTest {
            coEvery { api.vpnSignUp(any(), any()) } answers {
                lastArg<(VpnSignUpInformation?, List<AccountRequestError>) -> Unit>().invoke(
                    null,
                    listOf(AccountRequestError(600, "No internet connection")),
                )
            }

            val result =
                source.vpnSignup(
                    "data0",
                    "data1",
                    "data2",
                    "deviceId",
                )

            assertNull(result)

            // 1 initial attempt + 3 retries
            verify(exactly = 4) {
                api.vpnSignUp(any(), any())
            }
            coVerify(exactly = 3) {
                submitKpiEventUseCase.submitEvent(any())
            }
        }
}