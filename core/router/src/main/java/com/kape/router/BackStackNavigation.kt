package com.kape.router

import androidx.navigation3.runtime.NavKey
import com.kape.data.ComposeDestination
import com.kape.data.DestinationNavOptions

fun MutableList<NavKey>.navigateTo(destination: ComposeDestination) {
    when (val options = destination.navOptions) {
        is DestinationNavOptions.None -> Unit
        is DestinationNavOptions.PopUpTo -> popUpTo(options.destination, options.inclusive)
        DestinationNavOptions.ClearAll -> clear()
    }
    add(destination)
}

fun MutableList<NavKey>.navigateBack(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}

private fun MutableList<NavKey>.popUpTo(
    destination: ComposeDestination,
    inclusive: Boolean,
) {
    val index = indexOfLast { it::class == destination::class }
    if (index == -1) return
    val retainedSize = if (inclusive) index else index + 1
    while (size > retainedSize) {
        removeAt(lastIndex)
    }
}