package com.kape.signup.data

import com.kape.signup.domain.EmailDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EmailDataSourceImplTest {
    private val api: AndroidAccountAPI = mockk(relaxed = true)

    private lateinit var source: EmailDataSource

    @BeforeEach
    internal fun setUp() {
        source = EmailDataSourceImpl(api)
    }

    @Test
    fun `setEmail success`() =
        runTest {
            coEvery { api.setEmail(any(), any(), any()) } answers {
                lastArg<(String?, List<Error>) -> Unit>().invoke(null, emptyList())
            }
            val actual = source.setEmail("")
            assertTrue(actual)
        }

    @Test
    fun `setEmail fails`() =
        runTest {
            coEvery { api.setEmail(any(), any(), any()) } answers {
                lastArg<(String?, List<Error>) -> Unit>().invoke(null, listOf(Error()))
            }
            val actual = source.setEmail("")
            assertFalse(actual)
        }
}