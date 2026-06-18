package com.kape.contracts

import com.kape.localprefs.data.customization.ScreenElement
import kotlinx.coroutines.flow.StateFlow

interface ScreenElementProvider {
    val screenElements: StateFlow<List<ScreenElement>>
    val customizationElements: StateFlow<List<ScreenElement>>
}