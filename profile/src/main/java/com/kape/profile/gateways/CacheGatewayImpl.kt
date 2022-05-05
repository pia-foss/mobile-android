package com.kape.profile.gateways

import com.google.gson.Gson
import com.kape.profile.data.CacheDatasource
import com.kape.profile.models.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CacheGatewayImpl : CacheGateway, KoinComponent {
    private val cache: CacheDatasource by inject()

    override suspend fun getProfile(): Flow<Profile?> {
        return flow {
            val strProfile = cache.getString(PROFILE_KEY)
            emit(deserialize(strProfile))
        }
    }

    override suspend fun saveProfile(profile: Profile): Result<Unit> {
        cache.saveString(PROFILE_KEY, serialize(profile))
        return Result.success(Unit)
    }

    private fun serialize(profile: Profile): String {
        return Gson().toJson(profile)
    }

    private fun deserialize(s: String?): Profile? {
        return if (s == null) {
            null
        } else {
            Gson().fromJson(s, Profile::class.java)
        }
    }

    companion object {
        const val PROFILE_KEY = "profile"
    }
}