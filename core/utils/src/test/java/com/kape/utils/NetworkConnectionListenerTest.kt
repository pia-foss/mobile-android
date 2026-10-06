package com.kape.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.kape.contracts.NetworkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NetworkConnectionListenerTest {
    private val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    private val wifiManager = mockk<WifiManager>(relaxed = true)
    private val networkManager = mockk<NetworkManager>(relaxed = true)
    private val networkRequest = mockk<NetworkRequest>()
    private val context =
        mockk<Context> {
            every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
            every { getSystemService(Context.WIFI_SERVICE) } returns wifiManager
        }

    private val callbackSlot = slot<ConnectivityManager.NetworkCallback>()
    private val callback get() = callbackSlot.captured

    @BeforeEach
    fun setUp() {
        // The android.jar stubs return null from builder methods, so the chain has to return the builder itself.
        mockkConstructor(NetworkRequest.Builder::class)
        every { anyConstructed<NetworkRequest.Builder>().addCapability(any()) } answers { self as NetworkRequest.Builder }
        every { anyConstructed<NetworkRequest.Builder>().addTransportType(any()) } answers { self as NetworkRequest.Builder }
        every { anyConstructed<NetworkRequest.Builder>().build() } returns networkRequest
        every { connectivityManager.requestNetwork(networkRequest, capture(callbackSlot)) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(NetworkRequest.Builder::class)
    }

    @Test
    fun `init - requests the underlying non-VPN network instead of following the default network`() {
        createListener()

        verify { connectivityManager.requestNetwork(networkRequest, any<ConnectivityManager.NetworkCallback>()) }
        verify(exactly = 0) { connectivityManager.registerDefaultNetworkCallback(any()) }
        verify { anyConstructed<NetworkRequest.Builder>().addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) }
        verify { anyConstructed<NetworkRequest.Builder>().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
        verify { anyConstructed<NetworkRequest.Builder>().addTransportType(NetworkCapabilities.TRANSPORT_WIFI) }
        verify { anyConstructed<NetworkRequest.Builder>().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR) }
    }

    @Test
    fun `first network after start - handled with its SSID and not as a network change`() {
        val listener = createListener()

        callback.onCapabilitiesChanged(mockk(), wifiCapabilities(HOME_SSID))

        verify(exactly = 1) { networkManager.handleCurrentNetwork(HOME_SSID, true, false) }
        assertEquals(HOME_SSID, listener.ssid.value)
    }

    @Test
    fun `cellular network - handled with an empty SSID`() {
        createListener()

        callback.onCapabilitiesChanged(mockk(), cellularCapabilities())

        verify(exactly = 1) { networkManager.handleCurrentNetwork("", false, false) }
    }

    @Test
    fun `repeated capability changes on the same network - rules are applied only once`() {
        createListener()
        val network = mockk<Network>()

        repeat(5) { callback.onCapabilitiesChanged(network, wifiCapabilities(HOME_SSID)) }

        verify(exactly = 1) { networkManager.handleCurrentNetwork(any(), any(), any()) }
    }

    @Test
    fun `switching networks - each network is handled as a network change`() {
        createListener()

        callback.onCapabilitiesChanged(mockk(), wifiCapabilities(HOME_SSID))
        callback.onCapabilitiesChanged(mockk(), cellularCapabilities())
        callback.onCapabilitiesChanged(mockk(), wifiCapabilities(HOME_SSID))

        verifyOrder {
            networkManager.handleCurrentNetwork(HOME_SSID, true, false)
            networkManager.handleCurrentNetwork("", false, true)
            networkManager.handleCurrentNetwork(HOME_SSID, true, true)
        }
    }

    @Test
    fun `SSID resolved on the same network - handled again but not as a network change`() {
        createListener()
        val network = mockk<Network>()

        callback.onCapabilitiesChanged(network, wifiCapabilities(UNKNOWN_SSID))
        callback.onCapabilitiesChanged(network, wifiCapabilities(HOME_SSID))

        verifyOrder {
            networkManager.handleCurrentNetwork(UNKNOWN_SSID, true, false)
            networkManager.handleCurrentNetwork(HOME_SSID, true, false)
        }
    }

    @Test
    fun `triggerUpdate - re-applies rules for the unchanged network without reporting a network change`() {
        val listener = createListener()
        val network = mockk<Network>()
        callback.onCapabilitiesChanged(network, wifiCapabilities(HOME_SSID))

        listener.triggerUpdate()
        callback.onCapabilitiesChanged(network, wifiCapabilities(HOME_SSID))

        verifyOrder {
            connectivityManager.unregisterNetworkCallback(callback)
            connectivityManager.requestNetwork(networkRequest, callback)
        }
        verify(exactly = 2) { networkManager.handleCurrentNetwork(HOME_SSID, true, false) }
    }

    @Test
    fun `before Android 14 - SSID is read from WifiManager`() {
        val wifiInfo = mockk<WifiInfo> { every { ssid } returns HOME_SSID }
        every { wifiManager.connectionInfo } returns wifiInfo
        val listener = createListener(sdkInt = Build.VERSION_CODES.TIRAMISU)

        callback.onCapabilitiesChanged(mockk(), transportCapabilities(NetworkCapabilities.TRANSPORT_WIFI))

        verify(exactly = 1) { networkManager.handleCurrentNetwork(HOME_SSID, true, false) }
        assertEquals(HOME_SSID, listener.ssid.value)
    }

    @Test
    fun `before Android 14 - repeated capability changes on the same network are applied only once`() {
        val wifiInfo = mockk<WifiInfo> { every { ssid } returns HOME_SSID }
        every { wifiManager.connectionInfo } returns wifiInfo
        createListener(sdkInt = Build.VERSION_CODES.TIRAMISU)
        val network = mockk<Network>()

        repeat(3) { callback.onCapabilitiesChanged(network, transportCapabilities(NetworkCapabilities.TRANSPORT_WIFI)) }

        verify(exactly = 1) { networkManager.handleCurrentNetwork(any(), any(), any()) }
    }

    @Test
    fun `onLost without another network - reports disconnected`() {
        val listener = createListener()
        every { connectivityManager.activeNetwork } returns null

        callback.onLost(mockk())

        assertFalse(listener.isConnected.value)
    }

    @Test
    fun `onLost with another network that has internet - stays connected`() {
        val listener = createListener()
        val active = mockk<Network>()
        every { connectivityManager.activeNetwork } returns active
        every { connectivityManager.getNetworkCapabilities(active) } returns
            mockk { every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true }

        callback.onLost(mockk())

        assertTrue(listener.isConnected.value)
    }

    @Test
    fun `onAvailable after loss - reports connected`() {
        val listener = createListener()
        every { connectivityManager.activeNetwork } returns null
        callback.onLost(mockk())

        callback.onAvailable(mockk())

        assertTrue(listener.isConnected.value)
    }

    private fun createListener(sdkInt: Int = Build.VERSION_CODES.UPSIDE_DOWN_CAKE) =
        NetworkConnectionListener(context, networkManager, sdkInt)

    private fun wifiCapabilities(ssid: String): NetworkCapabilities {
        val wifiInfo = mockk<WifiInfo> { every { this@mockk.ssid } returns ssid }
        return transportCapabilities(NetworkCapabilities.TRANSPORT_WIFI).also {
            every { it.transportInfo } returns wifiInfo
        }
    }

    private fun cellularCapabilities(): NetworkCapabilities =
        transportCapabilities(NetworkCapabilities.TRANSPORT_CELLULAR).also {
            every { it.transportInfo } returns null
        }

    private fun transportCapabilities(transport: Int): NetworkCapabilities =
        mockk {
            every { hasTransport(any()) } answers { firstArg<Int>() == transport }
        }

    private companion object {
        const val HOME_SSID = "\"Home\""
        const val UNKNOWN_SSID = "<unknown ssid>"
    }
}