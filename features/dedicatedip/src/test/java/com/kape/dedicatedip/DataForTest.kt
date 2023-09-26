package com.kape.dedicatedip

import com.kape.dedicatedip.utils.DipApiResult
import com.kape.utils.ApiError
import com.kape.utils.ApiResult
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

open class DataForTest {
    companion object {
        @JvmStatic
        fun data() = Stream.of(
            Arguments.of(DipApiResult.Active),
            Arguments.of(DipApiResult.Invalid),
            Arguments.of(DipApiResult.Expired),
            Arguments.of(DipApiResult.Error),
        )
    }
}