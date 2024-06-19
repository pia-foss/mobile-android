package com.kape.dedicatedip.domain

class ObserveScreenCaptureUseCase {

    private val callbacks = mutableSetOf<(() -> Unit)>()

    fun registerCallback(callback: () -> Unit) {
        callbacks.add(callback)
    }

    fun announce() {
        callbacks.forEach {
            it()
        }
    }
}