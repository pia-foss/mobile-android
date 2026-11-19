package com.kape.csi.data

import com.kape.csi.domain.DiagnosticsDataSource
import com.privateinternetaccess.csi.CSIAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [DiagnosticsDataSource::class])
internal class CsiDiagnosticsDataSource(
    private val api: CSIAPI,
) : DiagnosticsDataSource {
    override suspend fun send(): String? =
        suspendCancellableCoroutine { continuation ->
            api.send { reportId, errors ->
                continuation.resume(reportId?.takeIf { errors.isEmpty() && it.isNotEmpty() })
            }
        }
}