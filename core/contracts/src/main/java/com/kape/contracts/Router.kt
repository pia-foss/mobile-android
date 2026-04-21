package com.kape.contracts

import com.kape.data.ComposeDestination
import kotlinx.coroutines.flow.StateFlow

interface Router {
    fun updateDestination(destination: ComposeDestination)

    fun getNavigationState(): StateFlow<ComposeDestination?>

    fun resetNavigation()

    fun navigateBack()

    fun getBackState(): StateFlow<Boolean>

    fun resetBack()
}