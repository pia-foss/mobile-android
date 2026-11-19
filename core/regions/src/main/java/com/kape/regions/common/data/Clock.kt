package com.kape.regions.common.data

import org.koin.core.annotation.Singleton

internal fun interface Clock {
    fun nowMillis(): Long
}

@Singleton(binds = [Clock::class])
internal class SystemTimeClock : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}