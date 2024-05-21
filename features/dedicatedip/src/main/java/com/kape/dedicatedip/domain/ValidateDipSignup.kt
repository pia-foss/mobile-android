package com.kape.dedicatedip.domain

import com.kape.dip.DipPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ValidateDipSignup(
    private val dipPrefs: DipPrefs,
    private val dataSource: DipDataSource,
) {

    fun signup(): Flow<Result<String>> = flow {
        dataSource.signup().collect { result ->
            result.fold(
                onSuccess = {
                    dipPrefs.setPurchasedSignupDipToken(it)
                },
                onFailure = { },
            )
            emit(result)
        }
    }
}