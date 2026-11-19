package com.kape.signup.domain

private val ISO_DURATION = Regex("^P(\\d+)([DWMY])$")

fun isoDurationToDays(duration: String?): Int? {
    if (duration == null) return null
    val match = ISO_DURATION.matchEntire(duration) ?: throw IllegalArgumentException("Unsupported duration format: $duration")
    val (value, unit) = match.destructured
    val days =
        when (unit) {
            "D" -> 1
            "W" -> 7
            "M" -> 30
            else -> 365
        }
    return value.toInt() * days
}