package com.kape.vpn.utils

import android.content.Context
import com.kape.contracts.LicenceReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedReader

class LicenceReaderImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) : LicenceReader {
    override suspend fun readLicences(): List<String> =
        withContext(ioDispatcher) {
            context.assets
                .open("acknowledgements.txt")
                .bufferedReader()
                .use(BufferedReader::readLines)
        }
}