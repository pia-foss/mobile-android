package com.kape.contracts

import kotlinx.coroutines.flow.StateFlow

interface SnoozeStatus {
    val snoozeEndTime: StateFlow<Long>

    fun cancelSnooze()
}