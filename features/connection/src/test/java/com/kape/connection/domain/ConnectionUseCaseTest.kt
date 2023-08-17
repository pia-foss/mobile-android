package com.kape.connection.domain

import android.app.Notification
import android.app.PendingIntent
import app.cash.turbine.test
import com.kape.utils.server.Server
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

internal class ConnectionUseCaseTest {

    private val source: ConnectionDataSource = mockk()
    private val server: Server = mockk()
    private val intent: PendingIntent = mockk()
    private val notification: Notification = mockk()
    private lateinit var useCase: ConnectionUseCase

    private val appModule = module {
        single { "certificate" }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule)
        }
        useCase = ConnectionUseCase(source)
    }

    @Test
    fun `startConnection - success`() = runTest {
        val expected = true
        every { source.startConnection(any(), any()) } returns flow {
            emit(expected)
        }
        every { source.getVpnToken() } returns "username:password"

        useCase.startConnection(server, intent, notification).test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `startConnection - failure`() = runTest {
        val expected = false
        every { source.startConnection(any(), any()) } returns flow {
            emit(expected)
        }
        every { source.getVpnToken() } returns "username:password"

        useCase.startConnection(server, intent, notification).test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `stopConnection - success`() = runTest {
        val expected = true
        every { source.stopConnection() } returns flow {
            emit(expected)
        }
        every { source.getVpnToken() } returns "username:password"

        useCase.stopConnection().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `stopConnection - failure`() = runTest {
        val expected = false
        every { source.stopConnection() } returns flow {
            emit(expected)
        }
        every { source.getVpnToken() } returns "username:password"

        useCase.stopConnection().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }
}