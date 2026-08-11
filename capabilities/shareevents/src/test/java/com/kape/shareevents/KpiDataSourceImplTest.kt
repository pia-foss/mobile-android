package com.kape.shareevents

import com.kape.contracts.KpiDataSource
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.shareevents.data.KpiDataSourceImpl
import com.kape.shareevents.data.KpiEventGenerator
import com.privateinternetaccess.kpi.KPIAPI
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class KpiDataSourceImplTest {
    private val api: KPIAPI = mockk(relaxed = true)
    private val eventGenerator: KpiEventGenerator = mockk(relaxed = true)

    private lateinit var source: KpiDataSource

    @BeforeEach
    internal fun setUp() {
        source = KpiDataSourceImpl(api, eventGenerator)
    }

    @Test
    fun `verify start calls api`() =
        runTest {
            source.start()
            verify(exactly = 1) { api.start() }
        }

    @Test
    fun `verify stop calls api`() =
        runTest {
            source.stop()
            verify(exactly = 1) { api.stop(any()) }
        }

    @Test
    fun `verify submit calls api`() =
        runTest {
            source.submitConnectionEvent(KpiConnectionEvent.ConnectionCancelled)
            verify(exactly = 1) { api.submit(any(), any()) }
        }

    @Test
    fun `verify flush calls api`() =
        runTest {
            source.flush()
            verify(exactly = 1) { api.flush(any()) }
        }

    @Test
    fun `verify recentEvents calls api`() =
        runTest {
            coEvery { api.recentEvents(any()) } answers {
                lastArg<(List<String>) -> Unit>().invoke(emptyList())
            }
            val actual = source.recentEvents()
            assertTrue(actual.isEmpty())
        }
}