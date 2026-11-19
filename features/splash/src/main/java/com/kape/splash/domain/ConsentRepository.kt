package com.kape.splash.domain

fun interface ConsentRepository {
    fun hasMadeConsentDecision(): Boolean
}