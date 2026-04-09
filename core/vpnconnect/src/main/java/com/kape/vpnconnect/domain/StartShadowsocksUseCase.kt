package com.kape.vpnconnect.domain

import com.kape.data.DI
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.domain.StartObfuscatorProcess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named

internal class StartShadowsocksUseCase(
    private val settingsPrefs: SettingsPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val startObfuscatorProcess: StartObfuscatorProcess,
    private val stopConnectionUseCase: StopConnectionUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) {
    private val ioScope = CoroutineScope(ioDispatcher)

    operator suspend fun invoke(): Boolean {
        if (settingsPrefs.isShadowsocksObfuscationEnabled().not()) return true

        val selectedShadowsocksServer =
            shadowsocksRegionPrefs.getSelectedShadowsocksServer() ?: return false

        val result = startObfuscatorProcess(
            obfuscatorProcessInformation = ObfuscatorProcessInformation(
                serverIp = selectedShadowsocksServer.host,
                serverPort = selectedShadowsocksServer.port.toString(),
                serverKey = selectedShadowsocksServer.key,
                serverEncryptMethod = selectedShadowsocksServer.cipher,
            ),
            obfuscatorProcessListener = object : ObfuscatorProcessListener {
                override fun processStopped() {
                    ioScope.launch { stopConnectionUseCase() }
                }
            },
        )
        return result.isSuccess
    }
}