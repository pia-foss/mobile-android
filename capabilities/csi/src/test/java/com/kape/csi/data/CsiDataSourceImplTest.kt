package com.kape.csi.data

import com.kape.csi.domain.CsiDataSource
import com.privateinternetaccess.csi.CSIAPI
import com.privateinternetaccess.csi.CSIInternalErrorCode
import com.privateinternetaccess.csi.CSIRequestError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CsiDataSourceImplTest {
    private val api: CSIAPI = mockk()
    private lateinit var csiDataSource: CsiDataSource

    @BeforeEach
    fun setUp() {
        csiDataSource = CsiDataSourceImpl(api)
    }

    @Test
    fun `send - success`() =
        runTest {
            val expected = "requestId"
            coEvery { api.send(any(), any()) } answers {
                lastArg<(String?, List<CSIRequestError>) -> Unit>().invoke(expected, emptyList())
            }
            val actual = csiDataSource.send()
            assertEquals(expected, actual)
        }

    @Test
    fun `send - failure`() =
        runTest {
            coEvery { api.send(any(), any()) } answers {
                lastArg<(String?, List<CSIRequestError>) -> Unit>().invoke(
                    null,
                    listOf(CSIRequestError(false, CSIInternalErrorCode.ERROR_HTTP_ENGINE, null, null)),
                )
            }
            val actual = csiDataSource.send()
            assertTrue(actual.isEmpty())
        }
}