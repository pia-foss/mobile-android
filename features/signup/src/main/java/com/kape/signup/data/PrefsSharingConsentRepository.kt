package com.kape.signup.data

import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.signup.domain.SharingConsentRepository
import org.koin.core.annotation.Singleton

@Singleton(binds = [SharingConsentRepository::class])
internal class PrefsSharingConsentRepository(
    private val prefs: ConsentPrefs,
) : SharingConsentRepository {
    override suspend fun setAllowSharing(allow: Boolean) {
        prefs.setAllowSharing(allow)
        prefs.setConsentDecisionMade(true)
    }

    override fun hasMadeDecision(): Boolean = prefs.hasMadeConsentDecision.value
}