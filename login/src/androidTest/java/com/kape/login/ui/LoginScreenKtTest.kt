package com.kape.login.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.kape.core.ApiError
import com.kape.core.ApiResult
import com.kape.login.domain.AuthenticationDataSource
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.login.domain.LoginUseCase
import com.kape.login.domain.LogoutUseCase
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.LoginWithEmailViewModel
import com.kape.router.Router
import com.kape.uicomponents.theme.PIATheme
import com.privateinternetaccess.account.AccountAPI
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest


class LoginScreenKtTest : KoinTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var context: Context
    private val accountAPI: AccountAPI = mockk(relaxed = true)


    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context.applicationContext
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loginAuthFailed() {
        val dataSource = mockk<AuthenticationDataSource>(relaxed = true) {
            every { isUserLoggedIn() } returns false
            every { login(any(), any()) } returns flow {
                emit(ApiResult.Error(ApiError.AuthFailed))
            }
        }
        val loginModule = module {
            single { dataSource }
            single { LoginUseCase(get()) }
            single { LogoutUseCase(get()) }
            single { GetUserLoggedInUseCase(get()) }
            viewModel { LoginViewModel(get(), get()) }
            viewModel { LoginWithEmailViewModel(get()) }
        }
        startKoin {
            modules(mutableListOf<Module>().apply {
                add(module {
                    single { context }
                    single { accountAPI }
                })
                add(loginModule)
            })
        }

        rule.setContent {
            PIATheme {
                LoginScreen(navController = rememberNavController())
            }
        }

        every { dataSource.isUserLoggedIn() } returns false

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithTag("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertIsDisplayed()
    }

    @Test
    fun loginThrottled() {
        val dataSource = mockk<AuthenticationDataSource>(relaxed = true) {
            every { isUserLoggedIn() } returns false
            every { login(any(), any()) } returns flow {
                emit(ApiResult.Error(ApiError.Throttled))
            }
        }
        val loginModule = module {
            single { dataSource }
            single { LoginUseCase(get()) }
            single { LogoutUseCase(get()) }
            single { GetUserLoggedInUseCase(get()) }
            viewModel { LoginViewModel(get(), get()) }
            viewModel { LoginWithEmailViewModel(get()) }
        }
        startKoin {
            modules(mutableListOf<Module>().apply {
                add(module {
                    single { context }
                    single { accountAPI }
                })
                add(loginModule)
            })
        }

        rule.setContent {
            PIATheme {
                LoginScreen(navController = rememberNavController())
            }
        }

        every { dataSource.isUserLoggedIn() } returns false

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithTag("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertIsDisplayed()
    }

    @Test
    fun loginAccountExpired() {
        val dataSource = mockk<AuthenticationDataSource>(relaxed = true) {
            every { isUserLoggedIn() } returns false
            every { login(any(), any()) } returns flow {
                emit(ApiResult.Error(ApiError.AccountExpired))
            }
        }
        val loginModule = module {
            single { dataSource }
            single { LoginUseCase(get()) }
            single { LogoutUseCase(get()) }
            single { GetUserLoggedInUseCase(get()) }
            viewModel { LoginViewModel(get(), get()) }
            viewModel { LoginWithEmailViewModel(get()) }
        }
        startKoin {
            modules(mutableListOf<Module>().apply {
                add(module {
                    single { context }
                    single { accountAPI }
                })
                add(loginModule)
            })
        }

        rule.setContent {
            PIATheme {
                LoginScreen(navController = rememberNavController())
            }
        }

        every { dataSource.isUserLoggedIn() } returns false

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithTag("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertIsDisplayed()
    }

    @Test
    fun loginSuccessful() {
        val dataSource = mockk<AuthenticationDataSource>(relaxed = true) {
            every { isUserLoggedIn() } returns false
            every { login(any(), any()) } returns flow {
                emit(ApiResult.Success)
            }
        }
        val loginModule = module {
            single { dataSource }
            single { LoginUseCase(get()) }
            single { LogoutUseCase(get()) }
            single { GetUserLoggedInUseCase(get()) }
            viewModel { LoginViewModel(get(), get()) }
            viewModel { LoginWithEmailViewModel(get()) }
        }
        startKoin {
            modules(mutableListOf<Module>().apply {
                add(module {
                    single { context }
                    single { Router() }
                })
                add(loginModule)
            })
        }

        rule.setContent {
            PIATheme {
                LoginScreen(navController = rememberNavController())
            }
        }

        every { dataSource.isUserLoggedIn() } returns false

        rule.onNodeWithContentDescription("Enter username").performTextInput("username")
        rule.onNodeWithContentDescription("Enter password").performTextInput("password")
        rule.onNodeWithContentDescription("LOGIN").performClick()
        rule.onNodeWithTag("errorMessage").assertDoesNotExist()
    }
}