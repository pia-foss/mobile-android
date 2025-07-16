package com.kape.customization

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.customization.prefs.CustomizationPrefs
import com.kape.router.ExitFlow
import com.kape.router.Router
import org.koin.core.component.KoinComponent

class CustomizationViewModel(private val prefs: CustomizationPrefs, private val router: Router) :
    ViewModel(), KoinComponent {

    private var items by mutableStateOf(prefs.getOrderedElements())

    fun exitCustomization() = router.handleFlow(ExitFlow.Customization)

    fun getOrderedElements(): List<ScreenElement> = items

    fun onMove(from: LazyListItemInfo, to: LazyListItemInfo) {
        items = items.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    fun saveOrder() {
        prefs.setOrderedElements(items)
        exitCustomization()
    }

    fun toggleVisibility(element: Element, isVisible: Boolean) {
        items.first { it.element == element }.apply {
            this.isVisible = isVisible
        }
        prefs.setOrderedElements(items)
    }
}