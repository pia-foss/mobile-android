package com.kape.appbar.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kape.ui.R
import org.koin.core.component.KoinComponent

class AppBarViewModel : ViewModel(), KoinComponent {

    // 09/08/2023 - Iva - hack the system
    var appBarText by mutableStateOf(R.string.menu)
        private set
    var accessibilityPrefix by mutableStateOf(R.string.menu)
        private set

}
