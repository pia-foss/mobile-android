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
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Named
import kotlin.coroutines.resume

internal class StartShadowsocksUseCase(
    private val settingsPrefs: SettingsPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val startObfuscatorProcess: StartObfuscatorProcess,
    private val stopConnectionUseCase: StopConnectionUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) {
    private val ioScope = CoroutineScope(ioDispatcher)

    operator suspend fun invoke(): Boolean = suspendCancellableCoroutine { cont ->

        if (!settingsPrefs.isShadowsocksObfuscationEnabled()) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }

        val selectedShadowsocksServer =
            shadowsocksRegionPrefs.getSelectedShadowsocksServer()

        if (selectedShadowsocksServer == null) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }

        ioScope.launch {
            val process = startObfuscatorProcess(
                obfuscatorProcessInformation = ObfuscatorProcessInformation(
                    serverIp = selectedShadowsocksServer.host,
                    serverPort = selectedShadowsocksServer.port.toString(),
                    serverKey = selectedShadowsocksServer.key,
                    serverEncryptMethod = selectedShadowsocksServer.cipher,
                ),
                obfuscatorProcessListener = object : ObfuscatorProcessListener {
                    override fun processStopped() {
                        if (cont.isActive) {
                            cont.resume(false)
                        }
                        stopConnectionUseCase.invoke(ioScope)
                    }
                },
            )

            // 👇 THIS is the key part
            cont.invokeOnCancellation {
                stopConnectionUseCase.invoke(ioScope)
            }

            cont.resume(process.isSuccess)
        }
    }
}