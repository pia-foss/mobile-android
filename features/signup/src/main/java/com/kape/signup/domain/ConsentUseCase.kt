package com.kape.signup.domain

import com.kape.localprefs.prefs.ConsentPrefs


class ConsentUseCase(private val prefs: ConsentPrefs) {

    fun setConsent(allowSharing: Boolean) {
        prefs.setAllowSharing(allowSharing)
    }

    fun getConsent() = prefs.getAllowSharing()
}