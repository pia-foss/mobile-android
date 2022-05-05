package com.kape.profile.data

import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow

interface NetworkDatasource {

    fun accountDetails(): Flow<Profile>

    companion object {
        const val DATE_FORMAT = "M dd, yyyy"
    }
}