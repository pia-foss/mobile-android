package com.kape.router

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RouterImpl : Router {
    private val _destination = MutableStateFlow<ComposeDestination?>(null)

    override fun updateDestination(destination: ComposeDestination) {
        _destination.update { destination }
    }

    override fun getNavigationState(): StateFlow<ComposeDestination?> {
        return _destination.asStateFlow()
    }

    override fun resetNavigation() {
        _destination.update { null }
    }
}