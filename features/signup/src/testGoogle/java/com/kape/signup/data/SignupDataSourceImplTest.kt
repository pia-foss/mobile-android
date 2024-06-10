package com.kape.signup.data

import app.cash.turbine.test
import com.kape.signup.data.models.Credentials
import com.kape.signup.di.signupModule
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.VpnSignUpInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

internal class SignupDataSourceImplTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)

    private lateinit var source: SignupDataSourceImpl

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, signupModule(appModule))
        }
        source = SignupDataSourceImpl(api)
    }

    @Test
    fun `signup success`() = runTest {
        val expected = Credentials("ok", "username", "password")
        val signupInfo = VpnSignUpInformation(expected.status, expected.username, expected.password)
        coEvery { api.vpnSignUp(any(), any()) } answers {
            lastArg<(VpnSignUpInformation?, List<Error>) -> Unit>().invoke(signupInfo, emptyList())
        }
        source.vpnSignup("orderId", "token", "productId").test {
            val actual = awaitItem()
            kotlin.test.assertEquals(expected, actual)
        }
    }

    @Test
    fun `signup fails`() = runTest {
        val expected = null
        coEvery { api.vpnSignUp(any(), any()) } answers {
            lastArg<(VpnSignUpInformation?, List<Error>) -> Unit>().invoke(expected, listOf(Error()))
        }
        source.vpnSignup("orderId", "token", "productId").test {
            val actual = awaitItem()
            kotlin.test.assertEquals(expected, actual)
        }
    }
}