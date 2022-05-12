package com.kape.login.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.LoginViewModel.Companion.EXPIRED
import com.kape.login.ui.vm.LoginViewModel.Companion.FAILED
import com.kape.login.ui.vm.LoginViewModel.Companion.SUCCESS
import com.kape.login.ui.vm.LoginViewModel.Companion.THROTTLED
import com.kape.uicomponents.theme.PIATheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest


class LoginScreenKtTest : KoinTest {

    @get:Rule
    val rule = createComposeRule()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loginAuthFailed() {
        val mockModule = module {
            viewModel {
                mockk<LoginViewModel>(relaxed = true) {
                    every { loginState } returns MutableStateFlow(FAILED)
                    every { loginState.value } returns FAILED
                }
            }
        }

        startKoin {
            modules(mockModule)
        }

        rule.setContent {
            PIATheme {
                LoginScreen()
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("Submit").performClick()
        rule.onNodeWithText("failed auth").assertIsDisplayed()
    }

    @Test
    fun loginThrottled() {
        val mockModule = module {
            viewModel {
                mockk<LoginViewModel>(relaxed = true) {
                    every { loginState } returns MutableStateFlow(THROTTLED)
                    every { loginState.value } returns THROTTLED
                }
            }
        }

        startKoin {
            modules(mockModule)
        }

        rule.setContent {
            PIATheme {
                LoginScreen()
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("Submit").performClick()
        rule.onNodeWithText("throttled").assertIsDisplayed()
    }

    @Test
    fun loginAccountExpired() {
        val mockModule = module {
            viewModel {
                mockk<LoginViewModel>(relaxed = true) {
                    every { loginState } returns MutableStateFlow(EXPIRED)
                    every { loginState.value } returns EXPIRED
                }
            }
        }

        startKoin {
            modules(mockModule)
        }

        rule.setContent {
            PIATheme {
                LoginScreen()
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("Submit").performClick()
        rule.onNodeWithText("expired account").assertIsDisplayed()
    }

    @Test
    fun loginSuccessful() {
        val mockModule = module {
            viewModel {
                mockk<LoginViewModel>(relaxed = true) {
                    every { loginState } returns MutableStateFlow(SUCCESS)
                    every { loginState.value } returns SUCCESS
                }
            }
        }

        startKoin {
            modules(mockModule)
        }

        rule.setContent {
            PIATheme {
                LoginScreen()
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("Submit").performClick()
        rule.onNodeWithText("failed auth").assertDoesNotExist()
    }

}