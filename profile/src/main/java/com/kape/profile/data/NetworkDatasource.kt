package com.kape.profile.data

import com.kape.core.ApiResult
import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow

interface NetworkDatasource {

    fun accountDetails(): Flow<Profile>
}