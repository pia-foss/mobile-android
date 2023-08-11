package com.kape.regionselection.utils

import com.kape.utils.server.Server

data class RegionSelectionScreenState(
    val idle: Boolean,
    val loading: Boolean,
    val regions: List<Server>,
    val showSortingOptions: Boolean = false,
)

val IDLE = RegionSelectionScreenState(idle = true, loading = false, regions = emptyList())
val LOADING = RegionSelectionScreenState(idle = false, loading = true, regions = emptyList())
fun showFilteringOptions(regions: List<Server>) = RegionSelectionScreenState(idle = false, loading = false, regions, true)

fun loaded(regions: List<Server>, showSort: Boolean = false) =
    RegionSelectionScreenState(idle = false, loading = false, regions, showSortingOptions = showSort)