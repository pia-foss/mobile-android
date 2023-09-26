package com.kape.dedicatedip.domain

import com.kape.dedicatedip.utils.DipApiResult
import kotlinx.coroutines.flow.Flow

interface DipDataSource {

    fun activate(ipToken: String): Flow<DipApiResult>

    fun renew(ipToken: String): Flow<DipApiResult>
}