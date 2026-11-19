package com.kape.localprefs.api

import kotlinx.coroutines.flow.StateFlow

interface ConsentStore {
    val allowSharing: StateFlow<Boolean>

    suspend fun setAllowSharing(allow: Boolean)
}