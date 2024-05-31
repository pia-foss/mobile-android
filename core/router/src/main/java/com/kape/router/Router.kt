package com.kape.router

import kotlinx.coroutines.flow.StateFlow

interface Router {

    fun handleFlow(flow: AppFlow)

    fun resetNavigation()

    fun getNavigation(): StateFlow<String>
}