package com.kape.csi.data

import com.kape.csi.domain.CsiDataSource
import com.privateinternetaccess.csi.CSIAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton([CsiDataSource::class])
class CsiDataSourceImpl(
    private val api: CSIAPI,
) : CsiDataSource {
    override suspend fun send(): String =
        suspendCancellableCoroutine { continuation ->
            api.send { reportIdentifier, listErrors ->
                if (listErrors.isNotEmpty()) {
                    continuation.resume("")
                    return@send
                }
                reportIdentifier?.let {
                    continuation.resume(it)
                } ?: run {
                    continuation.resume("")
                }
            }
        }
}