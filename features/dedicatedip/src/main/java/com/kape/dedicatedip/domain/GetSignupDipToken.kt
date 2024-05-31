package com.kape.dedicatedip.domain

import com.kape.dip.DipPrefs

class GetSignupDipToken(
    private val dipPrefs: DipPrefs,
) {
    operator fun invoke(): String =
        dipPrefs.getPurchasedSignupDipToken()
}