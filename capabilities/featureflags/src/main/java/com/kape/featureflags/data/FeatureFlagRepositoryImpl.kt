package com.kape.featureflags.data

import com.kape.featureflags.domain.FeatureFlagRepository
import com.kape.localprefs.prefs.FeaturePrefs
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

internal fun interface FeatureFlagRemoteDataSource {
    suspend fun fetchFlags(): List<String>?
}

@Singleton(binds = [FeatureFlagRemoteDataSource::class])
internal class AccountApiFeatureFlagRemoteDataSource(
    private val api: AndroidAccountAPI,
) : FeatureFlagRemoteDataSource {
    override suspend fun fetchFlags(): List<String>? =
        suspendCancellableCoroutine { continuation ->
            api.featureFlags { details, errors ->
                continuation.resume(if (errors.isNotEmpty()) null else details?.flags.orEmpty())
            }
        }
}

internal interface FeatureFlagLocalDataSource {
    fun observeFlags(): Flow<List<String>>

    suspend fun saveFlags(flags: List<String>)
}

@Singleton(binds = [FeatureFlagLocalDataSource::class])
internal class PrefsFeatureFlagLocalDataSource(
    private val prefs: FeaturePrefs,
) : FeatureFlagLocalDataSource {
    override fun observeFlags(): Flow<List<String>> = prefs.getFlags()

    override suspend fun saveFlags(flags: List<String>) = prefs.setFlags(flags)
}

@Singleton(binds = [FeatureFlagRepository::class])
internal class FeatureFlagRepositoryImpl(
    private val remote: FeatureFlagRemoteDataSource,
    private val local: FeatureFlagLocalDataSource,
) : FeatureFlagRepository {
    override fun observeFlags(): Flow<List<String>> = local.observeFlags()

    override suspend fun refresh() {
        remote.fetchFlags()?.let { local.saveFlags(it) }
    }
}