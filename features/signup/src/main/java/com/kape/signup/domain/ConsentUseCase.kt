package com.kape.signup.domain

import com.kape.localprefs.prefs.ConsentPrefs
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Singleton

@Singleton
class ConsentUseCase(
    private val prefs: ConsentPrefs,
) {
    suspend fun setConsent(allowSharing: Boolean) {
        prefs.setAllowSharing(allowSharing)
        setConsentDecisionMade(true)
    }

    suspend fun getConsent() = prefs.allowSharing.first()

    fun setConsentDecisionMade(made: Boolean) {
        prefs.setConsentDecisionMade(made)
    }
}