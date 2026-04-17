package com.kape.signup.data

import com.kape.signup.data.models.Credentials
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.SignUpInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals

internal class SignupDataSourceImplTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)

    private lateinit var source: SignupDataSourceImpl

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {}
        source = SignupDataSourceImpl(api)
    }

    @Test
    fun `signup success`() = runTest {
        val expected = Credentials("ok", "username", "password")
        val signupInfo = SignUpInformation(expected.status, expected.username, expected.password)
        coEvery { api.amazonSignUp(any(), any()) } answers {
            lastArg<(SignUpInformation?, List<Error>) -> Unit>().invoke(signupInfo, emptyList())
        }
        val actual = source.vpnSignup("userId", "receiptId", "email")
        assertEquals(expected, actual)
    }

    @Test
    fun `signup fails`() = runTest {
        coEvery { api.amazonSignUp(any(), any()) } answers {
            lastArg<(SignUpInformation?, List<Error>) -> Unit>().invoke(null, listOf(Error()))
        }
        val actual = source.vpnSignup("userId", "receiptId", "email")
        assertEquals(null, actual)
    }
}