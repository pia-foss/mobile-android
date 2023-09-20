package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.utils.ApiResult
import com.kape.utils.getApiError
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DipDataSourceImpl(private val accountApi: AndroidAccountAPI) : DipDataSource {

    override fun activate(ipToken: String): Flow<ApiResult> = callbackFlow {
        accountApi.dedicatedIPs(listOf(ipToken)) { details, errors ->
            if (errors.isNotEmpty()) {
                trySend(ApiResult.Error(getApiError(errors.last().code)))
                return@dedicatedIPs
            }
            // TODO: utilize dedicated IP utils https://polymoon.atlassian.net/browse/PIA-535
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