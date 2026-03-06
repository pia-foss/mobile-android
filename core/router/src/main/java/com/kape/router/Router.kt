package com.kape.router

import kotlinx.coroutines.flow.StateFlow


interface Router {

    fun updateDestination(destination: ComposeDestination)

    fun getNavigationState(): StateFlow<ComposeDestination?>

    fun resetNavigation()

}