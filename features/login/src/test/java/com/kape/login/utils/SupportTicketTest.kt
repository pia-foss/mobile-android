package com.kape.login.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SupportTicketTest {
    @Test
    fun `masks a standard pia username keeping first three and last two characters`() {
        assertEquals("p12****67", maskAccountIdentifier("p1234567"))
    }

    @Test
    fun `masks a short username without revealing more than one character`() {
        assertEquals("a****", maskAccountIdentifier("abcd"))
    }

    @Test
    fun `masking never returns the original username`() {
        val username = "p7654321"
        assertFalse(maskAccountIdentifier(username) == username)
    }

    @Test
    fun `description includes http status when error code is a real http response`() {
        val info = supportTicketInfo(errorCode = 500, errorMessage = "Internal Server Error")
        val description = buildSupportTicketDescription(info)
        assertTrue(description.contains("HTTP status: 500"))
    }

    @Test
    fun `description falls back to N-A when error details are missing`() {
        val info = supportTicketInfo(errorCode = null, errorMessage = null)
        val description = buildSupportTicketDescription(info)
        assertTrue(description.contains("Error code: N/A"))
        assertTrue(description.contains("Error message: N/A"))
    }

    @Test
    fun `url is prefixed with the helpdesk request endpoint and encoded fields`() {
        val info = supportTicketInfo(errorCode = 600, errorMessage = "Connection timed out [DNS]")
        val url = buildSupportTicketUrl(info)
        assertTrue(url.startsWith("https://helpdesk.privateinternetaccess.com/hc/en-us/requests/new?tf_subject="))
        assertTrue(url.contains("tf_subject=%5BAndroid%5D+Login+issue"))
        assertTrue(url.contains("tf_description="))
        assertFalse(url.contains(" "))
        assertFalse(url.contains("["))
        assertFalse(url.contains("]"))
    }

    @Test
    fun `url never includes the unmasked account identifier`() {
        val info = supportTicketInfo(errorCode = 600, errorMessage = null, maskedAccountId = "p12****67")
        val url = buildSupportTicketUrl(info)
        assertFalse(url.contains("p1234567"))
        assertTrue(url.contains("p12"))
    }

    private fun supportTicketInfo(
        errorCode: Int?,
        errorMessage: String?,
        maskedAccountId: String = "p12****67",
    ) = SupportTicketInfo(
        errorCode = errorCode,
        errorMessage = errorMessage,
        timestampUtc = "2026-07-29T14:23:11Z",
        appVersionName = "4.0.39",
        appVersionCode = 410039,
        osVersion = "14",
        manufacturer = "Google",
        model = "Pixel 8",
        maskedAccountId = maskedAccountId,
    )
}