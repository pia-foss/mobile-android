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
    }

    override suspend fun hasMadeDecision(): Boolean = prefs.hasMadeConsentDecisionNow()
}