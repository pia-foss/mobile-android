package com.kape.csi.data

import com.privateinternetaccess.csi.CSIAPI
import com.privateinternetaccess.csi.CSIRequestError
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class CsiDiagnosticsDataSourceTest {
    private var reportId: String? = null
    private var errors = emptyList<CSIRequestError>()
    private val api =
        object : CSIAPI {
            override fun send(
                shouldStoreLocally: Boolean,
                callback: (String?, List<CSIRequestError>) -> Unit,
            ) = callback(reportId, errors)
        }
    private val dataSource = CsiDiagnosticsDataSource(api)

    @Test
    fun `returns the report id`() =
        runTest {
            reportId = "report-1"

            assertEquals("report-1", dataSource.send())
        }

    @Test
    fun `fails when the api reports errors, even with a report id`() =
        runTest {
            reportId = "report-1"
            errors = listOf(mockk())

            assertNull(dataSource.send())
        }

    @Test
    fun `fails without a report id`() =
        runTest {
            reportId = null
            assertNull(dataSource.send())

            reportId = ""
            assertNull(dataSource.send())
        }
}