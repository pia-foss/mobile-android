package com.kape.login

import com.kape.login.utils.ApiError
import com.kape.login.utils.ApiResult
import com.privateinternetaccess.account.AccountRequestError
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

open class BaseTest {

    companion object {

        @JvmStatic
        fun accountApiResults() = Stream.of(
            Arguments.of(listOf(AccountRequestError(code = 600, message = null)), ApiResult.Error(ApiError.Unknown)),
            Arguments.of(listOf(AccountRequestError(code = 429, message = null)), ApiResult.Error(ApiError.Throttled)),
            Arguments.of(listOf(AccountRequestError(code = 401, message = null)), ApiResult.Error(ApiError.AuthFailed)),
            Arguments.of(listOf(AccountRequestError(code = 402, message = null)), ApiResult.Error(ApiError.AccountExpired)),
            Arguments.of(emptyList<AccountRequestError>(), ApiResult.Success)
        )

        @JvmStatic
        fun repoResults() = Stream.of(
            Arguments.of(null, ApiResult.Success),
            Arguments.of(ApiError.Unknown, ApiResult.Error(ApiError.Unknown)),
            Arguments.of(ApiError.Throttled, ApiResult.Error(ApiError.Throttled)),
            Arguments.of(ApiError.AuthFailed, ApiResult.Error(ApiError.AuthFailed)),
            Arguments.of(ApiError.AccountExpired, ApiResult.Error(ApiError.AccountExpired))
        )

        @JvmStatic
        fun booleans() =
            Stream.of(
                Arguments.of(true),
                Arguments.of(false)
            )
    }
}