package com.kape.shareevents.data.models

sealed class KpiConnectionSource(val value: String) {
    object Automatic : KpiConnectionSource("Automatic")
    object Manual : KpiConnectionSource("Manual")
}