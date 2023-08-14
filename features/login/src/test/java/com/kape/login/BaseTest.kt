package com.kape.login

import com.kape.utils.ApiError
import com.kape.utils.ApiResult
import com.privateinternetaccess.account.AccountRequestError
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.provider.Arguments
import org.koin.test.KoinTest
import java.util.stream.Stream

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseTest : KoinTest {

    @OptIn(DelicateCoroutinesApi::class)
    val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @AfterEach
    internal fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    companion object {

        @JvmStatic
        fun accountApiResults() = Stream.of(
            Arguments.of(listOf(AccountRequestError(code = 600, message = null)), ApiResult.Error(ApiError.Unknown)),
            Arguments.of(listOf(AccountRequestError(code = 429, message = null)), ApiResult.Error(ApiError.Throttled)),
            Arguments.of(listOf(AccountRequestError(code = 401, message = null)), ApiResult.Error(ApiError.AuthFailed)),
            Arguments.of(
                listOf(AccountRequestError(code = 402, message = null)),
                ApiResult.Error(
                    ApiError.AccountExpired
                )
            ),
            Arguments.of(emptyList<AccountRequestError>(), ApiResult.Success)
        )

        @JvmStatic
        fun booleans() =
            Stream.of(
                Arguments.of(true),
                Arguments.of(false)
            )
    }
}