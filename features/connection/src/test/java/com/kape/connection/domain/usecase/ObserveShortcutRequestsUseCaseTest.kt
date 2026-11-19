package com.kape.connection.domain.usecase

import com.kape.localprefs.api.ShortcutStore
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ObserveShortcutRequestsUseCaseTest {
    private val prefs =
        mockk<ShortcutStore>(relaxed = true) {
            every { isShortcutConnectToVpn } returns MutableStateFlow(false)
            every { isShortcutDisconnectVpn } returns MutableStateFlow(false)
            every { isShortcutChangeServer } returns MutableStateFlow(true)
            every { isShortcutSettings } returns MutableStateFlow(false)
        }

    @Test
    fun `a pending shortcut is emitted and marked handled`() =
        runTest {
            assertEquals(ShortcutRequest.ChangeServer, ObserveShortcutRequestsUseCase(prefs)().first())

            coVerify { prefs.setShortcutChangeServer(false) }
        }
}