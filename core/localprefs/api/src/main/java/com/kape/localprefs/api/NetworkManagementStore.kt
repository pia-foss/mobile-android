package com.kape.localprefs.api

import com.kape.networkmanagement.data.NetworkItem

interface NetworkManagementStore {
    suspend fun getAllRulesNow(): List<NetworkItem>
}