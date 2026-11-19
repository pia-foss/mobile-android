package com.kape.about.data

import android.content.Context
import com.kape.about.domain.LicenceRepository
import com.kape.contracts.di.Qualifiers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton(binds = [LicenceRepository::class])
class LicenceReader(
    private val context: Context,
    @Named(Qualifiers.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : LicenceRepository {
    override suspend fun readLicences(): List<String> =
        withContext(ioDispatcher) {
            context.assets
                .open("acknowledgements.txt")
                .bufferedReader()
                .use { it.readLines() }
        }
}