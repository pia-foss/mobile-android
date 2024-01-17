package com.kape.obfuscator.domain

import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.presenter.ObfuscatorProcessEventHandler

class StartObfuscatorProcessEventHandler : ObfuscatorProcessEventHandler {

    private lateinit var obfuscatorProcessListener: ObfuscatorProcessListener

    operator fun invoke(
        obfuscatorProcessListener: ObfuscatorProcessListener,
    ): ObfuscatorProcessEventHandler {
        this.obfuscatorProcessListener = obfuscatorProcessListener
        return this
    }

    // region ObfuscatorProcessEventHandler
    override fun processStopped(): Result<Unit> {
        obfuscatorProcessListener.processStopped()
        return Result.success(Unit)
    }
    // endregion
}