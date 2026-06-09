package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult

interface DipDataSource {
    suspend fun activate(ipToken: String): DipApiResult
}