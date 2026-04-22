package com.kape.customization

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kape.contracts.Router
import com.kape.customization.data.Element
import com.kape.data.ShadowsocksRegionSelection
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

@KoinViewModel
class CustomizationViewModel(
    private val prefs: CustomizationPrefs,
    private val settingsPrefs: SettingsPrefs,
    val router: Router,
) : ViewModel(),
    KoinComponent {
    private val items = mutableStateListOf<ScreenElement>()

    fun getOrderedElements(): List<ScreenElement> {
        val current = prefs.getElements()
        current.forEach {
            if (it.element == Element.ShadowsocksRegionSelection) {
                if (settingsPrefs.isShadowsocksObfuscationEnabled()) {
                    val visibleElement = it.apply { it.shouldDisplayElement = true }
                    items.add(visibleElement)
                } else {
                    val invisibleElement = it.apply { it.shouldDisplayElement = false }
                    items.add(invisibleElement)
                }
            } else {
                items.add(it)
            }
        }
        return items
    }

    fun onMove(
        from: LazyListItemInfo,
        to: LazyListItemInfo,
    ) {
        items.add(to.index, items.removeAt(from.index))
    }

    fun saveOrder() = prefs.setElements(items)

    fun toggleVisibility(
        element: Element,
        isVisible: Boolean,
    ) {
        items.first { it.element == element }.apply {
            this.isVisible = isVisible
        }
        prefs.setElements(items)
    }
}