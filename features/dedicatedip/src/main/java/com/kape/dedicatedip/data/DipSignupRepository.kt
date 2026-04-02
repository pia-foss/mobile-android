package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.dip.data.DedicatedIpSupportedCountries
import com.kape.localprefs.prefs.DipPrefs
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import org.koin.core.annotation.Singleton

@Singleton
class DipSignupRepository(
    private val dipDataSource: DipDataSource,
    private val dipPrefs: DipPrefs,
) {

    private companion object {
        private const val DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_1_DAY = 1L * 24 * 60 * 60 * 1000
        private const val DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_12_HOURS = 1L * 12 * 60 * 60 * 1000
    }

    suspend fun signupPlans(): AndroidAddonsSubscriptionsInformation? {
        dipPrefs.getDedicatedIpSignupPlans()?.let {
            if (System.currentTimeMillis() - it.persistedTimestamp > DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_1_DAY) {
                return fetchSignupPlans()
            } else {
                return it.signupPlans
            }
        }
        return fetchSignupPlans()
    }

    suspend fun dipSupportedCountries(): DipCountriesResponse? {
        dipPrefs.getDedicatedIpSupportedCountries()?.let {
            if (System.currentTimeMillis() - it.persistedTimestamp > DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_12_HOURS) {
                return fetchSupportedCountries()
            } else {
                return it.supportedCountries
            }
        }
        return fetchSupportedCountries()
    }

    // region private
    private suspend fun fetchSignupPlans(): AndroidAddonsSubscriptionsInformation? {
        val plans = dipDataSource.signupPlans()
        plans?.let {
            dipPrefs.setDedicatedIpSignupPlans(
                dedicatedIpSignupPlans = DedicatedIpSignupPlans(
                    persistedTimestamp = System.currentTimeMillis(),
                    signupPlans = it,
                ),
            )
        }
        return plans
    }

    private suspend fun fetchSupportedCountries(): DipCountriesResponse? {
        val countries = dipDataSource.supportedCountries()
        countries?.let {
            dipPrefs.setDedicatedIpSupportedCountries(
                dedicatedIpSupportedCountries = DedicatedIpSupportedCountries(
                    persistedTimestamp = System.currentTimeMillis(),
                    supportedCountries = it,
                ),
            )
        }
        return countries
    }
    // endregion
}