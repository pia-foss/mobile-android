package com.kape.profile.domain

import com.kape.profile.data.models.Profile

interface ProfileDatasource {

    suspend fun accountDetails(): Profile?

    suspend fun deleteAccount(): Boolean
}