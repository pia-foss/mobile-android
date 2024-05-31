package com.kape.csi.data

import app.cash.turbine.test
import com.kape.csi.domain.CsiDataSource
import com.privateinternetaccess.csi.CSIAPI
import com.privateinternetaccess.csi.CSIInternalErrorCode
import com.privateinternetaccess.csi.CSIRequestError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CsiDataSourceImplTest : KoinTest {
    private val api: CSIAPI = mockk()
    private lateinit var csiDataSource: CsiDataSource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule)
        }
        csiDataSource = CsiDataSourceImpl(api)
    }

    @Test
    fun `send - success`() = runTest {
        val expected = "requestId"
        coEvery { api.send(any(), any()) } answers {
            lastArg<(String?, List<CSIRequestError>) -> Unit>().invoke(
                expected,
                emptyList(),
            )
        }

        csiDataSource.send().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `send - failure`() = runTest {
        coEvery { api.send(any(), any()) } answers {
            lastArg<(String?, List<CSIRequestError>) -> Unit>().invoke(
                null,
                listOf(CSIRequestError(false, CSIInternalErrorCode.ERROR_HTTP_ENGINE, null, null)),
            )
        }

        csiDataSource.send().test {
            val actual = awaitItem()
            println(actual)
            assertTrue(actual.isEmpty())
        }
    }
}