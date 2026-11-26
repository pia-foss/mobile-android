package com.kape.login.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class LoginLinkTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://www.privateinternetaccess.com/login?token=abc",
            "https://www.privateinternetaccess.com/login/?foo=1&token=abc",
            "piavpn:login?token=abc",
            "piavpn:loginqr?token=abc",
            "piavpn:login?token=abc#fragment",
        ],
    )
    fun `reads the token from login links`(url: String) {
        assertEquals("abc", parseLoginLinkToken(url))
    }

    @ParameterizedTest
    @CsvSource(
        "https://example.com/login?token=abc",
        "https://www.privateinternetaccess.com/pricing?token=abc",
        "piavpn:settings?token=abc",
        "piavpn:login",
        "piavpn:login?token=",
    )
    fun `ignores urls that aren't login links or have no token`(url: String) {
        assertNull(parseLoginLinkToken(url))
    }

    @ParameterizedTest
    @CsvSource("piavpn:login?token=a%2Bb, a+b")
    fun `decodes the token`(
        url: String,
        expected: String,
    ) {
        assertEquals(expected, parseLoginLinkToken(url))
    }
}