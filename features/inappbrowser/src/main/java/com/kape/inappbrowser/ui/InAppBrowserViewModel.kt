package com.kape.inappbrowser.ui

import androidx.lifecycle.ViewModel
import com.kape.router.Back
import com.kape.router.Router
import org.koin.core.component.KoinComponent

class InAppBrowserViewModel(private val router: Router) : ViewModel(), KoinComponent {

    fun navigateBack() = router.handleFlow(Back)
}