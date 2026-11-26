package com.kape.localprefs.api

import kotlinx.coroutines.flow.Flow

interface DipStore {
    // mapping protocol (String) to port (int)
    val dedicatedIpPorts: Flow<List<Map<String, List<Int>>>>
}