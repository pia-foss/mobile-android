package com.kape.automation.data

import android.net.Network
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class NetworkChangeTest {
    private val home = mockk<Network>()
    private val mobile = mockk<Network>()

    @Test
    fun `the first network seen is handled, but isn't a change`() {
        assertEquals(false, networkChange(null, HandledNetwork(home, "Home", isWifi = true)))
    }

    @Test
    fun `the same network reported again is ignored`() {
        val wifi = HandledNetwork(home, "Home", isWifi = true)

        assertNull(networkChange(wifi, wifi.copy()))
    }

    @Test
    fun `switching to another network is a change`() {
        assertEquals(true, networkChange(HandledNetwork(home, "Home", true), HandledNetwork(mobile, "", false)))
    }

    @Test
    fun `the same network with a new name is handled again, but isn't a change`() {
        assertEquals(false, networkChange(HandledNetwork(home, "", true), HandledNetwork(home, "Home", true)))
    }
}