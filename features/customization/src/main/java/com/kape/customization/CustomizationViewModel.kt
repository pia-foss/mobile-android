package com.kape.customization

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kape.contracts.Router
import com.kape.customization.data.Element
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.localprefs.prefs.CustomizationPrefs
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

@KoinViewModel
class CustomizationViewModel(private val prefs: CustomizationPrefs, val router: Router) :
    ViewModel(), KoinComponent {

    private var items by mutableStateOf(prefs.getOrderedElements())

    fun getOrderedElements(): List<ScreenElement> = items

    fun onMove(from: LazyListItemInfo, to: LazyListItemInfo) {
        items = items.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    fun saveOrder() = prefs.setOrderedElements(items)

    fun toggleVisibility(element: Element, isVisible: Boolean) {
        items.first { it.element == element }.apply {
            this.isVisible = isVisible
        }
        prefs.setOrderedElements(items)
    }
}