package com.kape.router

import com.kape.contracts.Router
import com.kape.data.ComposeDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Singleton

@Singleton(binds = [Router::class])
class RouterImpl : Router {
    private val _destination = MutableStateFlow<ComposeDestination?>(null)
    private val _navigateBack = MutableStateFlow(false)

    override fun updateDestination(destination: ComposeDestination) {
        _destination.update { destination }
    }

    override fun getNavigationState(): StateFlow<ComposeDestination?> = _destination.asStateFlow()

    override fun resetNavigation() {
        _destination.update { null }
    }

    override fun navigateBack() {
        _navigateBack.update { true }
    }

    override fun getBackState(): StateFlow<Boolean> = _navigateBack.asStateFlow()

    override fun resetBack() {
        _navigateBack.update { false }
    }
}