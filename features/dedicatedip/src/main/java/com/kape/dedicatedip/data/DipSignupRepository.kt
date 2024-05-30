package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dip.DipPrefs
import com.kape.dip.data.DedicatedIpSignupPlans
import com.kape.dip.data.FetchedDedicatedIpSignupPlansMock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DipSignupRepository(
    private val dipDataSource: DipDataSource,
    private val dipPrefs: DipPrefs,
) {

    private companion object {
        private const val DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_180_DAYS = 180L * 24 * 60 * 60 * 1000
    }

    fun signupPlans(): Flow<FetchedDedicatedIpSignupPlansMock> = callbackFlow {
        dipPrefs.getDedicatedIpSignupPlans()?.let {
            if (System.currentTimeMillis() - it.persistedTimestamp > DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_180_DAYS) {
                fetchSignupPlans().collect { fetchedSignupPlans ->
                    trySend(fetchedSignupPlans)
                }
            } else {
                trySend(it.signupPlans)
            }
        } ?: run {
            fetchSignupPlans().collect {
                trySend(it)
            }
        }
        awaitClose { channel.close() }
    }

    // region private
    private fun fetchSignupPlans(): Flow<FetchedDedicatedIpSignupPlansMock> = callbackFlow {
        dipDataSource.signupPlans().collect {
            dipPrefs.setDedicatedIpSignupPlans(
                dedicatedIpSignupPlans = DedicatedIpSignupPlans(
                    persistedTimestamp = System.currentTimeMillis(),
                    signupPlans = it,
                ),
            )
            trySend(it)
        }
        awaitClose { channel.close() }
    }
    // endregion
}