package com.kape.dedicatedip.data

import com.kape.data.DI
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.localprefs.prefs.DipPrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([DipDataSource::class])
class DipDataSourceImpl(
    private val accountApi: AndroidAccountAPI,
    private val dipPrefs: DipPrefs,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : DipDataSource {
    override suspend fun activate(ipToken: String): DipApiResult =
        suspendCancellableCoroutine { cont ->
            accountApi.redeemDedicatedIPs(listOf(ipToken)) { details, errors ->
                if (errors.isNotEmpty()) {
                    cont.resume(DipApiResult.Error)
                    return@redeemDedicatedIPs
                }
                val activated = details.firstOrNull { it.dipToken == ipToken }
                activated?.let {
                    when (it.status) {
                        DedicatedIPInformationResponse.Status.active -> {
                            ioScope.launch {
                                dipPrefs.addDedicatedIp(it)
                                cont.resume(DipApiResult.Active)
                            }
                        }

                        DedicatedIPInformationResponse.Status.expired -> {
                            cont.resume(DipApiResult.Expired)
                        }

                        DedicatedIPInformationResponse.Status.invalid -> {
                            cont.resume(DipApiResult.Invalid)
                        }

                        DedicatedIPInformationResponse.Status.error -> {
                            cont.resume(DipApiResult.Error)
                        }
                    }
                } ?: run {
                    cont.resume(DipApiResult.Error)
                }
            }
        }
}