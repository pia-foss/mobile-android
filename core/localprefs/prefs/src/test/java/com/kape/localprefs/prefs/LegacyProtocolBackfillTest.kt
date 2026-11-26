package com.kape.localprefs.prefs

import com.kape.settings.data.VpnProtocols
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class LegacyProtocolBackfillTest {
    @ParameterizedTest(name = "freshInstall={0}, alreadyPinned={1}, explicitSelection={2}")
    @CsvSource(
        "true, false, false",
        "false, true, false",
        "false, false, true",
        "true, true, false",
        "true, false, true",
        "false, true, true",
        "true, true, true",
    )
    fun `leaves the protocol alone when the install is new, already pinned, or has a user choice`(
        isFreshInstall: Boolean,
        alreadyPinned: Boolean,
        hasExplicitSelection: Boolean,
    ) {
        assertNull(resolveLegacyProtocolBackfill(isFreshInstall, alreadyPinned, hasExplicitSelection))
    }

    @Test
    fun `pins WireGuard for an upgraded install that never chose a protocol`() {
        assertEquals(
            VpnProtocols.WireGuard,
            resolveLegacyProtocolBackfill(isFreshInstall = false, alreadyPinned = false, hasExplicitSelection = false),
        )
    }
}