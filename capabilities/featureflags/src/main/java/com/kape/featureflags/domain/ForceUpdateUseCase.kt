package com.kape.featureflags.domain

import com.kape.localprefs.prefs.FeaturePrefs
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Singleton

@Singleton
class ForceUpdateUseCase(
    private val featurePrefs: FeaturePrefs,
) {
    suspend fun requiresForceUpdate(): Boolean = featurePrefs.getFlags().first().contains("force-update-required")
}