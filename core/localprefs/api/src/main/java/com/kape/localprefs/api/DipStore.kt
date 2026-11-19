package com.kape.localprefs.api

import kotlinx.coroutines.flow.Flow

interface DipStore {
    val dedicatedIpPorts: Flow<List<Map<String, List<Int>>>>
}