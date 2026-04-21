package com.kape.login

import com.kape.data.auth.ApiError
import com.kape.data.auth.ApiResult
import com.privateinternetaccess.account.AccountRequestError
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

open class BaseTest {

    companion object {

        @JvmStatic
        fun accountApiResults() = Stream.of(
            Arguments.of(
                listOf(AccountRequestError(code = 600, message = null)),
                ApiResult.Error(ApiError.Unknown),
            ),
            Arguments.of(
                listOf(AccountRequestError(code = 429, message = null)),
                ApiResult.Error(ApiError.Throttled),
            ),
            Arguments.of(
                listOf(AccountRequestError(code = 401, message = null)),
                ApiResult.Error(ApiError.AuthFailed),
            ),
            Arguments.of(
                listOf(AccountRequestError(code = 402, message = null)),
                ApiResult.Error(ApiError.AccountExpired),
            ),
            Arguments.of(emptyList<AccountRequestError>(), ApiResult.Success),
        )

        @JvmStatic
        fun tokens() = Stream.of(
            Arguments.of(null, null, false),
            Arguments.of("", null, false),
            Arguments.of("apiToken", "", false),
            Arguments.of("apiToken", null, false),
            Arguments.of("apiToken", "vpnToken", true),
        )

        @JvmStatic
        fun booleans() =
            Stream.of(
                Arguments.of(true),
                Arguments.of(false),
            )
    }
}