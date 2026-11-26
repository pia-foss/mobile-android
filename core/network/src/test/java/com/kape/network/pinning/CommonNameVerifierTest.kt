package com.kape.network.pinning

import com.kape.network.pinning.EndpointIdentity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.security.auth.x500.X500Principal

class CommonNameVerifierTest {
    private val verifier =
        CommonNameVerifier(
            listOf(
                EndpointIdentity("10.0.0.1", "server-a"),
                EndpointIdentity("10.0.0.2", "server-b"),
            ),
        )

    @Test
    fun `accepts known endpoint with its expected common name`() {
        assertTrue(verifier.matches("10.0.0.1", "server-a"))
        assertTrue(verifier.matches("10.0.0.2", "server-b"))
    }

    @Test
    fun `rejects common name belonging to a different endpoint`() {
        assertFalse(verifier.matches("10.0.0.1", "server-b"))
    }

    @Test
    fun `rejects unknown endpoint`() {
        assertFalse(verifier.matches("10.0.0.3", "server-a"))
    }

    @Test
    fun `rejects missing common name`() {
        assertFalse(verifier.matches("10.0.0.1", null))
    }

    @Test
    fun `rejects everything when no endpoints are known`() {
        assertFalse(CommonNameVerifier(emptyList()).matches("10.0.0.1", "server-a"))
    }

    @Test
    fun `extracts first common name from principal`() {
        assertEquals("server-a", commonNameOf(X500Principal("CN=server-a, O=PIA, C=US")))
    }

    @Test
    fun `returns null when principal has no common name`() {
        assertNull(commonNameOf(X500Principal("O=PIA, C=US")))
    }
}