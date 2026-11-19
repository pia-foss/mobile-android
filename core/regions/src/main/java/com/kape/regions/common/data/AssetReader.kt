package com.kape.regions.common.data

import android.content.Context
import org.koin.core.annotation.Singleton

internal fun interface AssetReader {
    fun read(fileName: String): String
}

@Singleton(binds = [AssetReader::class])
internal class AndroidAssetReader(
    private val context: Context,
) : AssetReader {
    override fun read(fileName: String): String =
        context.assets
            .open(fileName)
            .bufferedReader()
            .use { it.readText() }
}

internal fun String.withoutSignature(): String = substringBefore("\n\n")