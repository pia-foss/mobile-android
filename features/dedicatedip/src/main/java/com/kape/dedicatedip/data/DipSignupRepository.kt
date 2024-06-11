package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dip.DipPrefs
import com.kape.dip.data.DedicatedIpSignupPlans
import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DipSignupRepository(
    private val dipDataSource: DipDataSource,
    private val dipPrefs: DipPrefs,
) {

    private companion object {
        private const val DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_1_DAY = 1L * 24 * 60 * 60 * 1000
    }

    fun signupPlans(): Flow<AndroidAddonsSubscriptionsInformation?> = callbackFlow {
        dipPrefs.getDedicatedIpSignupPlans()?.let {
            if (System.currentTimeMillis() - it.persistedTimestamp > DIP_SIGNUP_FETCH_PLANS_MIN_CACHE_1_DAY) {
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
    private fun fetchSignupPlans(): Flow<AndroidAddonsSubscriptionsInformation?> = callbackFlow {
        dipDataSource.signupPlans().collect {
            it?.let {
                dipPrefs.setDedicatedIpSignupPlans(
                    dedicatedIpSignupPlans = DedicatedIpSignupPlans(
                        persistedTimestamp = System.currentTimeMillis(),
                        signupPlans = it,
                    ),
                )
            }
            trySend(it)
        }
        awaitClose { channel.close() }
    }
    // endregion
}