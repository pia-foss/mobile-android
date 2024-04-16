package com.kape.about.vm

import androidx.lifecycle.ViewModel
import com.kape.router.ExitFlow
import com.kape.router.Router
import org.koin.core.component.KoinComponent

class AboutViewModel(private val router: Router, val licences: List<String>) :
    ViewModel(),
    KoinComponent {

    fun navigateBack() = router.handleFlow(ExitFlow.About)
}