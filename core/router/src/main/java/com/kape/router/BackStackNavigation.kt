package com.kape.router

import androidx.navigation3.runtime.NavKey
import com.kape.data.ComposeDestination
import com.kape.data.DestinationNavOptions

/**
 * Applies [destination] to the back stack, honouring its [DestinationNavOptions]
 * the same way Navigation 2's `popUpTo` / `launchSingleTop` did.
 */
fun MutableList<NavKey>.navigateTo(destination: ComposeDestination) {
    when (val options = destination.navOptions) {
        is DestinationNavOptions.None -> Unit
        is DestinationNavOptions.PopUpTo -> popUpTo(options.destination, options.inclusive)
        DestinationNavOptions.ClearAll -> clear()
    }
    add(destination)
}

/**
 * Removes the top entry, unless it is the only one left.
 *
 * @return true if an entry was removed.
 */
fun MutableList<NavKey>.navigateBack(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}

// Routes are matched by type, as Navigation 2 did for type-safe routes.
// If the route is not on the stack nothing is popped.
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