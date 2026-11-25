package com.kape.splash.data

import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.splash.domain.ConsentRepository
import org.koin.core.annotation.Singleton

@Singleton(binds = [ConsentRepository::class])
internal class PrefsConsentRepository(
    private val consentPrefs: ConsentPrefs,
) : ConsentRepository {
    override suspend fun hasMadeConsentDecision(): Boolean = consentPrefs.hasMadeConsentDecisionNow()
}