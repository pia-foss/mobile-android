package com.kape.profile.ui

data class ProfileScreenState(
    val loading: Boolean,
    val username: String,
    val expirationDate: String,
    val expired: Boolean,
)

val IDLE = ProfileScreenState(loading = false, username = "", expirationDate = "", expired = false)
val LOADING = ProfileScreenState(loading = true, username = "", expirationDate = "", expired = false)

fun createSuccessState(username: String, expirationDate: String, expired: Boolean) = ProfileScreenState(
    loading = false,
    username = username,
    expirationDate = expirationDate,
    expired = expired,
)