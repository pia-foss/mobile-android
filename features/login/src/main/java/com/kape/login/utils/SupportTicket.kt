package com.kape.login.utils

import java.net.URLEncoder

private const val HELPDESK_URL = "https://helpdesk.privateinternetaccess.com/hc/en-us/requests/new"
private const val SUPPORT_TICKET_SUBJECT = "[Android] Login issue"
private const val MASKED_PREFIX_LENGTH = 3
private const val MASKED_SUFFIX_LENGTH = 2

data class SupportTicketInfo(
    val errorCode: Int?,
    val errorMessage: String?,
    val timestampUtc: String,
    val appVersionName: String,
    val appVersionCode: Int,
    val osVersion: String,
    val manufacturer: String,
    val model: String,
    val maskedAccountId: String,
)

fun maskAccountIdentifier(username: String): String {
    val trimmed = username.trim()
    if (trimmed.length <= MASKED_PREFIX_LENGTH + MASKED_SUFFIX_LENGTH) {
        return "${trimmed.take(1)}****"
    }
    return "${trimmed.take(MASKED_PREFIX_LENGTH)}****${trimmed.takeLast(MASKED_SUFFIX_LENGTH)}"
}

fun buildSupportTicketDescription(info: SupportTicketInfo): String {
    val lines =
        mutableListOf(
            "Error code: ${info.errorCode ?: "N/A"}",
            "Error message: ${info.errorMessage ?: "N/A"}",
        )
    if (info.errorCode != null) {
        lines += "HTTP status: ${info.errorCode}"
    }
    lines +=
        listOf(
            "Timestamp (UTC): ${info.timestampUtc}",
            "App version: ${info.appVersionName} (${info.appVersionCode})",
            "Android OS version: ${info.osVersion}",
            "Device: ${info.manufacturer} ${info.model}",
            "Account: ${info.maskedAccountId}",
        )
    return lines.joinToString("\n")
}

fun buildSupportTicketUrl(info: SupportTicketInfo): String {
    val subject = URLEncoder.encode(SUPPORT_TICKET_SUBJECT, "UTF-8")
    val description = URLEncoder.encode(buildSupportTicketDescription(info), "UTF-8")
    return "$HELPDESK_URL?tf_subject=$subject&tf_description=$description"
}