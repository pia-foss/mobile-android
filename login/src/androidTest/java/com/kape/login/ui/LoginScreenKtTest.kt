package com.kape.login.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.kape.login.testLoginModule
import com.kape.login.ui.theme.PIATheme
import com.kape.login.ui.vm.LoginViewModel
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.assertEquals


class LoginScreenKtTest : KoinTest {

    @get:Rule
    val rule = createComposeRule()

    private val viewModel: LoginViewModel = mockk()


    @Before
    fun setUp() {
        startKoin {
            modules(testLoginModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loginSuccessful() {

        rule.setContent {
            PIATheme {
                LoginScreen()
            }
        }

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("Submit").performClick()
        assertEquals("failed auth", viewModel.loginState.value.error)

    }
}