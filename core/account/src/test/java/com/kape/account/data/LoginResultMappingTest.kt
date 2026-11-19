package com.kape.account.data

import com.kape.account.domain.LoginResult
import com.privateinternetaccess.account.AccountRequestError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class LoginResultMappingTest {
    private fun error(code: Int) = AccountRequestError(code, "message $code")

    @Test
    fun `no errors is a success`() {
        assertEquals(LoginResult.Success, emptyList<AccountRequestError>().toLoginResult())
    }

    @Test
    fun `maps account errors by HTTP status`() {
        assertEquals(LoginResult.Failed, listOf(error(401)).toLoginResult())
        assertEquals(LoginResult.Expired, listOf(error(402)).toLoginResult())
        assertEquals(LoginResult.Throttled, listOf(error(429)).toLoginResult())
    }

    @Test
    fun `anything else is a service failure, from the last error`() {
        assertEquals(
            LoginResult.ServiceUnavailable(503, "message 503"),
            listOf(error(401), error(503)).toLoginResult(),
        )
    }
}