package com.kape.dedicatedip.domain

import com.kape.utils.ApiResult
import kotlinx.coroutines.flow.Flow

interface DipDataSource {

    fun activate(ipToken: String): Flow<ApiResult>

    fun renew(ipToken: String): Flow<ApiResult>
}