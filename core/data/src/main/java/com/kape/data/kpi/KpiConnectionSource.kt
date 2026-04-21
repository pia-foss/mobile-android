package com.kape.data.kpi

sealed class KpiConnectionSource(
    val value: String,
) {
    data object Automatic : KpiConnectionSource("Automatic")

    data object Manual : KpiConnectionSource("Manual")
}