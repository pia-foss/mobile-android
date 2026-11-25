package com.kape.splash.domain

fun interface ConsentRepository {
    suspend fun hasMadeConsentDecision(): Boolean
}