package com.kape.profile.ui

import com.kape.profile.R
import com.kape.uicomponents.components.UiText

data class ProfileScreenState(
    val loading: Boolean,
    val username: UiText? = null,
    val expirationMessage: UiText? = null,
    val expirationDate: UiText? = null,
    val errorMessage: UiText? = null
)

val IDLE = ProfileScreenState(loading = false)
val LOADING = ProfileScreenState(loading = true)
val NO_PROFILE = ProfileScreenState(
    loading = false,
    errorMessage = UiText.StringResource(R.string.error_no_profile)
)

fun createSuccessState(expirationMessage: UiText?, expirationDate: UiText?) = ProfileScreenState(
    expirationMessage = expirationMessage,
    expirationDate = expirationDate,
    loading = false
)
