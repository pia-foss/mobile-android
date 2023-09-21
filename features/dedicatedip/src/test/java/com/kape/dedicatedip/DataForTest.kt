package com.kape.dedicatedip

import com.kape.utils.ApiError
import com.kape.utils.ApiResult
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

open class DataForTest {
    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(ApiResult.Success, true),
            Arguments.of(ApiResult.Error(ApiError.AuthFailed), false),
            Arguments.of(ApiResult.Error(ApiError.AccountExpired), false),
            Arguments.of(ApiResult.Error(ApiError.Throttled), false),
            Arguments.of(ApiResult.Error(ApiError.Unknown), false),
        )
    }
}