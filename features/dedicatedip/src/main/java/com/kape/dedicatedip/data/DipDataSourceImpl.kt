package com.kape.dedicatedip.data

import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DipDataSourceImpl(
    private val accountApi: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
) : DipDataSource {

    override fun activate(ipToken: String): Flow<DipApiResult> = callbackFlow {
        accountApi.dedicatedIPs(listOf(ipToken)) { details, errors ->
            if (errors.isNotEmpty()) {
                trySend(DipApiResult.Error)
                return@dedicatedIPs
            }
            val activated = details.firstOrNull { it.dipToken == ipToken }
            activated?.let {
                when (it.status) {
                    DedicatedIPInformationResponse.Status.active -> {
                        dipPrefs.addDedicatedIp(it)
                        trySend(DipApiResult.Active)
                    }

                    DedicatedIPInformationResponse.Status.expired -> {
                        trySend(DipApiResult.Expired)
                    }

                    DedicatedIPInformationResponse.Status.invalid -> {
                        trySend(DipApiResult.Invalid)
                    }

                    DedicatedIPInformationResponse.Status.error -> {
                        trySend(DipApiResult.Error)
                    }
                }
            } ?: run {
                trySend(DipApiResult.Error)
            }
        }
        awaitClose { channel.close() }
    }

    override fun renew(ipToken: String): Flow<DipApiResult> = callbackFlow {
        accountApi.renewDedicatedIP(ipToken) {
            if (it.isNotEmpty()) {
                trySend(DipApiResult.Error)
                return@renewDedicatedIP
            }
            // todo: update if needed once implemented
            trySend(DipApiResult.Active)
        }
        awaitClose { channel.close() }
    }
}