package com.kape.localprefs.api

import com.kape.vpnconnect.data.AutoProtocolNudgeState

interface AutoProtocolNudgeStore {
    suspend fun getStateNow(): AutoProtocolNudgeState

    suspend fun setState(state: AutoProtocolNudgeState)
}