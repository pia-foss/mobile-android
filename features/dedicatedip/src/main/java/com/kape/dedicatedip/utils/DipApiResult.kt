package com.kape.dedicatedip.utils

sealed class DipApiResult {
    data object Active : DipApiResult()
    data object Invalid : DipApiResult()
    data object Expired : DipApiResult()
    data object Error : DipApiResult()
}