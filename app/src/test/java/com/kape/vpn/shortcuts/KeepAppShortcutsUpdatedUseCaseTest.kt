package com.kape.vpn.shortcuts

import com.kape.contracts.ConnectionStatusSource
import com.kape.data.ConnectionStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class KeepAppShortcutsUpdatedUseCaseTest {
    @Test
    fun `republishes when the VPN connects or disconnects, not on every status change`() =
        runTest {
            val status = MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
            val published = mutableListOf<Boolean>()
            val source = mockk<ConnectionStatusSource> { every { this@mockk.status } returns status }

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                KeepAppShortcutsUpdatedUseCase({ published += it }, source)()
            }
            status.value = ConnectionStatus.CONNECTING
            status.value = ConnectionStatus.CONNECTED
            status.value = ConnectionStatus.DISCONNECTING
            status.value = ConnectionStatus.DISCONNECTED

            assertEquals(listOf(false, true, false), published)
        }
}