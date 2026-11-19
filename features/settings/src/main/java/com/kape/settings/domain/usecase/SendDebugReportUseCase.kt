package com.kape.settings.domain.usecase

import com.kape.contracts.DebugLogCleaner
import com.kape.csi.domain.SendDiagnosticsUseCase
import com.kape.localprefs.api.CsiStore
import com.kape.vpnconnect.domain.GetLogsUseCase
import org.koin.core.annotation.Factory

@Factory
class SendDebugReportUseCase(
    private val getLogs: GetLogsUseCase,
    private val csiPrefs: CsiStore,
    private val sendDiagnostics: SendDiagnosticsUseCase,
    private val logCleaner: DebugLogCleaner,
) {
    suspend operator fun invoke(): String? {
        csiPrefs.setProtocolDebugLogs(getLogs.getDebugLogs().joinToString(separator = "\n"))
        val reportId = sendDiagnostics()
        csiPrefs.clearProtocolDebugLogs()
        if (!reportId.isNullOrEmpty()) logCleaner.clearDebugLogs()
        return reportId
    }
}