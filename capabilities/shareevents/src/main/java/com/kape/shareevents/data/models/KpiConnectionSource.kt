package com.kape.shareevents.data.models

sealed class KpiConnectionSource(val value: String) {
    data object Automatic : KpiConnectionSource("Automatic")
    data object Manual : KpiConnectionSource("Manual")
}