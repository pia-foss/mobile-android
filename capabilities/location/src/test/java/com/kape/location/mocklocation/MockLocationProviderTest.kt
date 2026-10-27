package com.kape.location.mocklocation

import android.content.Context
import android.location.LocationManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// Build.VERSION.SDK_INT is 0 in unit tests, so the pre-S provider list and addTestProvider overload are used.
class MockLocationProviderTest {
    private val locationManager: LocationManager = mockk(relaxed = true)
    private val fusedLocationMocker: FusedLocationMocker = mockk(relaxed = true)
    private val context: Context = mockk()

    @BeforeEach
    fun setUp() {
        every { context.getSystemService(LocationManager::class.java) } returns locationManager
        every { locationManager.allProviders } returns listOf(GPS, NETWORK, LocationManager.PASSIVE_PROVIDER)
    }

    @Test
    fun `push registers the standard providers and enables fused mock mode`() =
        runTest {
            val provider = createProvider()

            assertTrue(provider.push(LATITUDE, LONGITUDE))

            listOf(GPS, NETWORK).forEach { name ->
                verify { locationManager.addTestProvider(name, any(), any(), any(), any(), any(), any(), any(), any(), any()) }
                verify { locationManager.setTestProviderEnabled(name, true) }
            }
            verify { fusedLocationMocker.setMockMode(true) }
        }

    @Test
    fun `push skips gps_hardware when the device does not have it`() =
        runTest {
            createProvider().push(LATITUDE, LONGITUDE)

            verify(exactly = 0) {
                locationManager.addTestProvider(GPS_HARDWARE, any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `push registers gps_hardware when the device has it`() =
        runTest {
            every { locationManager.allProviders } returns listOf(GPS, GPS_HARDWARE, NETWORK)

            createProvider().push(LATITUDE, LONGITUDE)

            verify {
                locationManager.addTestProvider(GPS_HARDWARE, any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `push returns false when PIA is not the selected mock location app`() =
        runTest {
            every {
                locationManager.addTestProvider(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } throws SecurityException()
            val provider = createProvider()

            assertFalse(provider.push(LATITUDE, LONGITUDE))
            runCurrent()

            verify(exactly = 0) { fusedLocationMocker.setMockMode(any()) }
            verify(exactly = 0) { locationManager.setTestProviderLocation(any(), any()) }
        }

    @Test
    fun `pushes in a 3s burst every 50ms then once per second`() =
        runTest {
            createProvider().push(LATITUDE, LONGITUDE)

            advanceAndRun(BURST_DURATION_MS)
            assertRounds(BURST_ROUNDS)

            advanceAndRun(PUSH_INTERVAL_MS - 1)
            assertRounds(BURST_ROUNDS)

            advanceAndRun(1)
            assertRounds(BURST_ROUNDS + 1)
        }

    @Test
    fun `each round pushes to every provider and the fused mocker`() =
        runTest {
            createProvider().push(LATITUDE, LONGITUDE)
            runCurrent()

            verify(exactly = 1) { locationManager.setTestProviderLocation(GPS, any()) }
            verify(exactly = 1) { locationManager.setTestProviderLocation(NETWORK, any()) }
            verify(exactly = 1) { fusedLocationMocker.setMockLocation(any()) }
        }

    @Test
    fun `a new location restarts the burst`() =
        runTest {
            val provider = createProvider()
            provider.push(LATITUDE, LONGITUDE)
            advanceAndRun(STEADY_START_MS)
            val roundsBefore = BURST_ROUNDS + 2 // the burst plus the steady rounds at 4s and 5s
            assertRounds(roundsBefore)

            provider.push(OTHER_LATITUDE, OTHER_LONGITUDE)
            // The pump picks up the new target at its next steady round, then bursts from there.
            advanceAndRun(PUSH_INTERVAL_MS + 500)

            assertRounds(roundsBefore + 1 + 500 / BURST_INTERVAL_MS.toInt())
        }

    @Test
    fun `pushing the same location again does not restart the burst`() =
        runTest {
            val provider = createProvider()
            provider.push(LATITUDE, LONGITUDE)
            advanceAndRun(STEADY_START_MS)
            val roundsBefore = BURST_ROUNDS + 2

            provider.push(LATITUDE, LONGITUDE)
            advanceAndRun(PUSH_INTERVAL_MS + 500)

            assertRounds(roundsBefore + 1)
        }

    @Test
    fun `a failing provider does not stop the others`() =
        runTest {
            every { locationManager.setTestProviderLocation(GPS, any()) } throws SecurityException()

            createProvider().push(LATITUDE, LONGITUDE)
            advanceAndRun(BURST_DURATION_MS)

            verify(exactly = BURST_ROUNDS) { locationManager.setTestProviderLocation(NETWORK, any()) }
        }

    @Test
    fun `stop cancels the pump, removes the providers and disables fused mock mode`() =
        runTest {
            val provider = createProvider()
            provider.push(LATITUDE, LONGITUDE)
            runCurrent()

            provider.stop()
            advanceAndRun(STEADY_START_MS)

            assertRounds(1)
            listOf(GPS, NETWORK).forEach { name ->
                verify { locationManager.setTestProviderEnabled(name, false) }
                // Once before registering, once on stop.
                verify(exactly = 2) { locationManager.removeTestProvider(name) }
            }
            verify { fusedLocationMocker.setMockMode(false) }
        }

    @Test
    fun `stop removes the providers even when disabling them fails`() =
        runTest {
            every { locationManager.setTestProviderEnabled(any(), false) } throws IllegalArgumentException()
            val provider = createProvider()
            provider.push(LATITUDE, LONGITUDE)

            provider.stop()

            listOf(GPS, NETWORK).forEach { name ->
                verify(exactly = 2) { locationManager.removeTestProvider(name) }
            }
        }

    @Test
    fun `stop before any push does nothing`() =
        runTest {
            createProvider().stop()

            verify(exactly = 0) { locationManager.removeTestProvider(any()) }
            verify(exactly = 0) { fusedLocationMocker.setMockMode(any()) }
        }

    // The pump loops forever, so it runs in backgroundScope, which runTest cancels at the end.
    private fun TestScope.createProvider() = MockLocationProvider(context, fusedLocationMocker, backgroundScope)

    // advanceTimeBy doesn't run tasks scheduled exactly at the target time, so run those too.
    private fun TestScope.advanceAndRun(millis: Long) {
        advanceTimeBy(millis)
        runCurrent()
    }

    private fun assertRounds(rounds: Int) {
        verify(exactly = rounds) { locationManager.setTestProviderLocation(GPS, any()) }
        verify(exactly = rounds) { fusedLocationMocker.setMockLocation(any()) }
    }

    private companion object {
        const val GPS = LocationManager.GPS_PROVIDER
        const val NETWORK = LocationManager.NETWORK_PROVIDER
        const val GPS_HARDWARE = "gps_hardware"

        const val LATITUDE = 44.43
        const val LONGITUDE = 26.10
        const val OTHER_LATITUDE = 52.52
        const val OTHER_LONGITUDE = 13.40

        // Mirrors the private timing constants in MockLocationProvider.
        const val PUSH_INTERVAL_MS = 1_000L
        const val BURST_INTERVAL_MS = 50L
        const val BURST_ITERATIONS = 60
        const val BURST_DURATION_MS = BURST_ITERATIONS * BURST_INTERVAL_MS

        // A round at t=0, then one after each of the 60 burst delays.
        const val BURST_ROUNDS = BURST_ITERATIONS + 1

        // Two steady rounds past the end of the burst (at 4s and 5s).
        const val STEADY_START_MS = BURST_DURATION_MS + 2 * PUSH_INTERVAL_MS
    }
}