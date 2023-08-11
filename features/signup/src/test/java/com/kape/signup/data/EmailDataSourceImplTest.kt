package com.kape.signup.data

import app.cash.turbine.test
import com.kape.signup.di.signupModule
import com.kape.signup.domain.EmailDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class EmailDataSourceImplTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)

    private lateinit var source: EmailDataSource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, signupModule)
        }
        source = EmailDataSourceImpl()
    }

    @Test
    fun `setEmail success`() = runTest {
        coEvery { api.setEmail(any(), any(), any()) } answers {
            lastArg<(String?, List<Error>) -> Unit>().invoke(null, emptyList())
        }
        source.setEmail("").test {
            val actual = awaitItem()
            assertTrue(actual)
        }
    }

    @Test
    fun `setEmail fails`() = runTest {
        coEvery { api.setEmail(any(), any(), any()) } answers {
            lastArg<(String?, List<Error>) -> Unit>().invoke(null, listOf(Error()))
        }
        source.setEmail("").test {
            val actual = awaitItem()
            assertFalse(actual)
        }
    }
}