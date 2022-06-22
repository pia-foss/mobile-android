package com.kape.profile.ui

data class ProfileScreenState(
    val loading: Boolean,
    val errorMessage: String? = null
)

val IDLE = ProfileScreenState(loading = false)
val LOADING = ProfileScreenState(loading = true)
val SUCCESS = ProfileScreenState(loading = false)
val ERROR_NO_PROFILE = ProfileScreenState(loading = false, errorMessage = "TODO error message no profile")