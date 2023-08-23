package com.kape.shareevents

import app.cash.turbine.test
import com.kape.shareevents.data.KpiDataSourceImpl
import com.kape.shareevents.data.models.KpiConnectionEvent
import com.kape.shareevents.data.models.KpiConnectionSource
import com.kape.shareevents.di.kpiModule
import com.kape.shareevents.domain.KpiDataSource
import com.privateinternetaccess.kpi.KPIAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertTrue

internal class KpiDataSourceImplTest {

    private val api: KPIAPI = mockk(relaxed = true)
    private val prefs: KpiPrefs = mockk()
    private val userAgent: String = "user agent"

    private lateinit var source: KpiDataSource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, kpiModule(appModule))
        }
        source = KpiDataSourceImpl(prefs, userAgent, api)
    }

    @Test
    fun `verify start calls api`() = runTest {
        source.start()
        verify(exactly = 1) { api.start() }
    }

    @Test
    fun `verify stop calls api`() = runTest {
        source.start()
        verify(exactly = 1) { api.start() }
    }

    @Test
    fun `verify submit calls api`() = runTest {
        every { prefs.getActiveProtocol() } returns ""
        source.submit(KpiConnectionEvent.ConnectionEstablished, KpiConnectionSource.Automatic)
        verify(exactly = 1) { api.submit(any(), any()) }
    }

    @Test
    fun `verify flush calls api`() = runTest {
        source.flush()
        verify(exactly = 1) { api.flush(any()) }
    }

    @Test
    fun `verify recentEvents calls api`() = runTest {
        coEvery { api.recentEvents(any()) } answers {
            lastArg<(List<String>) -> Unit>().invoke(emptyList())
        }
        source.recentEvents().test {
            val actual = awaitItem()
            assertTrue(actual.isEmpty())
        }
    }
}