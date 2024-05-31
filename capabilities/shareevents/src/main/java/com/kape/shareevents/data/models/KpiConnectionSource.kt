package com.kape.shareevents.data.models

import com.kape.utils.KoverIgnore

@KoverIgnore("Sealed class, no need for test coverage")
sealed class KpiConnectionSource(val value: String) {
    data object Automatic : KpiConnectionSource("Automatic")
    data object Manual : KpiConnectionSource("Manual")
}