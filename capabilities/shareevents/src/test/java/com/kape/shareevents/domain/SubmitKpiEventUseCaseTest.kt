package com.kape.shareevents.domain

import android.os.SystemClock
import com.kape.shareevents.data.models.KpiConnectionEvent
import com.kape.shareevents.data.models.KpiConnectionSource
import com.kape.shareevents.data.models.KpiConnectionStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.test.KoinTest
import java.util.stream.Stream

class SubmitKpiEventUseCaseTest : KoinTest {
    private val api: KpiDataSource = mockk()
    private lateinit var useCase: SubmitKpiEventUseCase

    @BeforeEach
    fun setUp() {
        useCase = SubmitKpiEventUseCase(api)
    }

    @ParameterizedTest(name = "status: {0}, isManual: {1}")
    @MethodSource("data")
    fun testSubmit(
        status: KpiConnectionStatus,
        isManualConnection: Boolean,
        event: KpiConnectionEvent,
    ) {
        every { api.submit(any(), any()) } returns Unit
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } returns 0
        val expected =
            if (isManualConnection) KpiConnectionSource.Manual else KpiConnectionSource.Automatic
        if (status == KpiConnectionStatus.NotConnected) {
            useCase.submitConnectionEvent(KpiConnectionStatus.Connecting, isManualConnection)
        }
        useCase.submitConnectionEvent(status, isManualConnection)
        verify { api.submit(event, expected) }
    }

    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(
                KpiConnectionStatus.Connected,
                true,
                KpiConnectionEvent.ConnectionEstablished,
            ),
            Arguments.of(
                KpiConnectionStatus.Connecting,
                true,
                KpiConnectionEvent.ConnectionAttempt,
            ),
            Arguments.of(
                KpiConnectionStatus.NotConnected,
                true,
                KpiConnectionEvent.ConnectionCancelled,
            ),
            Arguments.of(
                KpiConnectionStatus.Connected,
                false,
                KpiConnectionEvent.ConnectionEstablished,
            ),
            Arguments.of(
                KpiConnectionStatus.Connecting,
                false,
                KpiConnectionEvent.ConnectionAttempt,
            ),
            Arguments.of(
                KpiConnectionStatus.NotConnected,
                false,
                KpiConnectionEvent.ConnectionCancelled,
            ),
        )
    }
}