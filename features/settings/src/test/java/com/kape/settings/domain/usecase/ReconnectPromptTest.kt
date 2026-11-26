package com.kape.settings.domain.usecase

import com.kape.contracts.ConnectionInfoProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Job
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ReconnectPromptTest {
    private var connected = true
    private val reconnect = mockk<ReconnectUseCase> { every { this@mockk() } returns Job() }
    private val prompt = ReconnectPrompt(mockk<ConnectionInfoProvider> { every { isConnected() } answers { connected } }, reconnect)

    @Test
    fun `a changed setting asks to reconnect while connected`() {
        assertTrue(prompt.onSettingChanged())
        assertTrue(prompt.isVisible.value)
    }

    @Test
    fun `nothing is asked while disconnected`() {
        connected = false

        assertFalse(prompt.onSettingChanged())
    }

    @Test
    fun `reconnecting hides the prompt and reconnects`() {
        prompt.onSettingChanged()

        prompt.onReconnect()

        assertFalse(prompt.isVisible.value)
        verify { reconnect() }
    }

    @Test
    fun `later hides the prompt without reconnecting`() {
        prompt.onSettingChanged()

        prompt.onLater()

        assertFalse(prompt.isVisible.value)
        verify(exactly = 0) { reconnect() }
    }
}