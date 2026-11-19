package com.kape.login.domain

import java.net.URLDecoder

private const val APP_LINK_PREFIX = "https://www.privateinternetaccess.com/login"
private const val LEGACY_LINK_PREFIX = "piavpn:login"

fun parseLoginLinkToken(url: String): String? {
    if (!url.startsWith(APP_LINK_PREFIX) && !url.startsWith(LEGACY_LINK_PREFIX)) return null
    return url
        .substringAfter('?', missingDelimiterValue = "")
        .substringBefore('#')
        .split('&')
        .map { it.split('=', limit = 2) }
        .firstOrNull { it.first() == "token" }
        ?.getOrNull(1)
        ?.let { URLDecoder.decode(it, "UTF-8") }
        ?.takeIf { it.isNotEmpty() }
}