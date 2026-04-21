package com.kape.signup.domain

import com.kape.localprefs.prefs.ConsentPrefs
import org.koin.core.annotation.Singleton

@Singleton
class ConsentUseCase(
    private val prefs: ConsentPrefs,
) {
    fun setConsent(allowSharing: Boolean) {
        prefs.setAllowSharing(allowSharing)
    }

    fun getConsent() = prefs.getAllowSharing()
}