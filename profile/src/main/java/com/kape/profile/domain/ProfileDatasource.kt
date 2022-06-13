package com.kape.profile.domain

import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileDatasource {

    fun accountDetails(): Flow<Profile>
}