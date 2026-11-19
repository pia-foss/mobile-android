package com.kape.data.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ApiResultTest {
    @Test
    fun `maps the known http codes to their errors`() {
        assertEquals(ApiError.AuthFailed, getApiError(401))
        assertEquals(ApiError.AccountExpired, getApiError(402))
        assertEquals(ApiError.Throttled, getApiError(429))
    }

    @Test
    fun `any other or missing code is unknown`() {
        assertEquals(ApiError.Unknown, getApiError(500))
        assertEquals(ApiError.Unknown, getApiError(200))
        assertEquals(ApiError.Unknown, getApiError(null))
    }

    @Test
    fun `an error result prints only its error`() {
        assertEquals("ApiError.Throttled", ApiResult.Error(ApiError.Throttled, 429, "slow down").toString())
        assertEquals("ApiResult.Success", ApiResult.Success.toString())
    }
}