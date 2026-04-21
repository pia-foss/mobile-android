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
    private val destination = MutableStateFlow<ComposeDestination?>(null)
    private val navigateBack = MutableStateFlow(false)

    override fun updateDestination(destination: ComposeDestination) {
        this.destination.update { destination }
    }

    override fun getNavigationState(): StateFlow<ComposeDestination?> = destination.asStateFlow()

    override fun resetNavigation() {
        destination.update { null }
    }

    override fun navigateBack() {
        navigateBack.update { true }
    }

    override fun getBackState(): StateFlow<Boolean> = navigateBack.asStateFlow()

    override fun resetBack() {
        navigateBack.update { false }
    }
}