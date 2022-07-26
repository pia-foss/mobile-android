package com.kape.signup.data

import app.cash.turbine.test
import com.kape.signup.di.signupModule
import com.kape.signup.models.Credentials
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.SignUpInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
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
            modules(appModule, signupModule)
        }
        source = SignupDataSourceImpl()
    }

    @Test
    fun `signup success`() = runTest {
        val expected = Credentials("ok", "username", "password")
        val signupInfo = SignUpInformation(expected.status, expected.username, expected.password)
        coEvery { api.amazonSignUp(any(), any()) } answers {
            lastArg<(SignUpInformation?, List<Error>) -> Unit>().invoke(signupInfo, emptyList())
        }
        source.signup("userId", "receiptId").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `signup fails`() = runTest {
        val expected = null
        coEvery { api.amazonSignUp(any(), any()) } answers {
            lastArg<(SignUpInformation?, List<Error>) -> Unit>().invoke(expected, listOf(Error()))
        }
        source.signup("userId", "receiptId").test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }
}