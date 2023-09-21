package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dip.DipPrefs
import com.kape.utils.ApiResult
import com.kape.utils.getApiError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DipDataSourceImpl(
    private val accountApi: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
) : DipDataSource {

    override fun activate(ipToken: String): Flow<ApiResult> = callbackFlow {
        accountApi.dedicatedIPs(listOf(ipToken)) { details, errors ->
            if (errors.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(errors.last().code)))
                return@dedicatedIPs
            }
            for (dip in details) {
                if (dip.status == DedicatedIPInformationResponse.Status.active) {
                    dipPrefs.addDedicatedIp(dip)
                }
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }

    override fun renew(ipToken: String): Flow<ApiResult> = callbackFlow {
        accountApi.renewDedicatedIP(ipToken) {
            if (it.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(it.last().code)))
                return@renewDedicatedIP
            }
            trySend(ApiResult.Success)
        }
        awaitClose { channel.close() }
    }
}