package com.kape.share_events.models

sealed class KpiConnectionSource(val value: String) {
    object Automatic : KpiConnectionSource("Automatic")
    object Manual : KpiConnectionSource("Manual")
}
