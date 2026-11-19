package com.kape.login.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SupportTicketTest {
    private val info =
        SupportTicketInfo(
            errorCode = 503,
            errorMessage = "Unavailable",
            timestampUtc = "2026-11-17T10:00:00Z",
            appVersionName = "1.0",
            appVersionCode = 1,
            osVersion = "16",
            manufacturer = "Google",
            model = "Pixel",
            maskedAccountId = "p00****89",
        )

    @Test
    fun `masks the middle of the username`() {
        assertEquals("p00****89", maskAccountIdentifier("  p0012345689 "))
        assertEquals("a****", maskAccountIdentifier("abcde"))
    }

    @Test
    fun `describes the failure, app and device`() {
        val description = buildSupportTicketDescription(info)

        assertTrue("Error code: 503" in description)
        assertTrue("HTTP status: 503" in description)
        assertTrue("App version: 1.0 (1)" in description)
        assertTrue("Device: Google Pixel" in description)
        assertTrue("Account: p00****89" in description)
    }

    @Test
    fun `leaves out the HTTP status when there is no error code`() {
        assertFalse("HTTP status" in buildSupportTicketDescription(info.copy(errorCode = null)))
    }

    @Test
    fun `prefills the helpdesk form`() {
        val url = buildSupportTicketUrl(info)

        assertTrue(url.startsWith("https://helpdesk.privateinternetaccess.com/hc/en-us/requests/new?tf_subject="))
        assertTrue("tf_description=Error+code%3A+503" in url)
    }
}