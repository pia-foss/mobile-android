package com.kape.login.ui.vm

import com.kape.login.BaseTest
import com.kape.login.domain.LoginUseCase
import com.kape.login.utils.LoginState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


@OptIn(ExperimentalCoroutinesApi::class)
internal class LoginViewModelTest : BaseTest() {

    private val useCase: LoginUseCase = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    internal fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        viewModel = LoginViewModel(useCase)
    }

    @Test
    fun loginSuccessful() = runTest {
        coEvery { useCase.login(any(), any()) } returns flow {
            emit(LoginState.Successful)
        }
        launch(Dispatchers.Main) {  // Will be launched in the mainThreadSurrogate dispatcher
            viewModel.login("any", "any").invokeOnCompletion {
                val loginScreenState = viewModel.loginState.value
                assertTrue(loginScreenState.flowCompleted)
                assertTrue(loginScreenState.idle)
                assertFalse(loginScreenState.loading)
                assertEquals(null, loginScreenState.error)
            }
        }
    }
}