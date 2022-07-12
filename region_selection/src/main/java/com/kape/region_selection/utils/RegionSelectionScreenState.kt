package com.kape.region_selection.utils

import com.kape.region_selection.server.Server

data class RegionSelectionScreenState(
    val idle: Boolean,
    val loading: Boolean,
    val regions: List<Server>,
    val showSortingOptions: Boolean = false,
)

val IDLE = RegionSelectionScreenState(idle = true, loading = false, regions = emptyList())
val LOADING = RegionSelectionScreenState(idle = false, loading = true, regions = emptyList())
fun showFilteringOptions(regions: List<Server>) = RegionSelectionScreenState(idle = false, loading = false, regions, true)

fun loaded(regions: List<Server>) = RegionSelectionScreenState(idle = false, loading = false, regions)