package com.kape.profile.domain

import com.kape.profile.data.models.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileDatasource {

    fun accountDetails(): Flow<Profile?>

    fun deleteAccount(): Flow<Boolean>
}