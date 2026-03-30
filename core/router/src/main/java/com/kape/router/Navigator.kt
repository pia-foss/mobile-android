package com.kape.router

import com.kape.contracts.Router
import com.kape.contracts.data.ComposeDestination

class Navigator(private val router: Router) {

    fun navigateTo(destination: ComposeDestination) {
        router.updateDestination(destination)
    }

    fun navigateBack() {
        router.navigateBack()
    }
}