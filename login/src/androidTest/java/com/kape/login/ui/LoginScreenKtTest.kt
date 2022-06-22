package com.kape.login.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.EXPIRED
import com.kape.login.utils.FAILED
import com.kape.login.utils.SUCCESS
import com.kape.login.utils.THROTTLED
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
                LoginScreen(rememberNavController())
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithTag("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertIsDisplayed()
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
                LoginScreen(rememberNavController())
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertIsDisplayed()
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
                LoginScreen(rememberNavController())
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertIsDisplayed()
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
                LoginScreen(rememberNavController())
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertDoesNotExist()
    }
}