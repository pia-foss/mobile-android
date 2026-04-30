package com.kape.contracts

interface LicenceReader {
    suspend fun readLicences(): List<String>
}