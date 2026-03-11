package com.kape.router

class Navigator(
    private val router: Router,
) {
    fun navigateTo(destination: ComposeDestination) {
        router.updateDestination(destination)
    }

    fun navigateBack() {
        router.navigateBack()
    }
}