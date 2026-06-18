package com.kape.customization

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.contracts.ScreenElementProvider
import com.kape.customization.data.Element
import com.kape.data.DI
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.localprefs.prefs.CustomizationPrefs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent

@KoinViewModel
class CustomizationViewModel(
    private val prefs: CustomizationPrefs,
    private val screenElementProvider: ScreenElementProvider,
    val router: Router,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(),
    KoinComponent {
    val items = mutableStateListOf<ScreenElement>()

    init {
        viewModelScope.launch {
            screenElementProvider.customizationElements.collectLatest {
                items.clear()
                items.addAll(it)
            }
        }
    }

    fun onMove(
        from: LazyListItemInfo,
        to: LazyListItemInfo,
    ) {
        items.add(to.index, items.removeAt(from.index))
    }

    fun saveOrder() =
        viewModelScope.launch(ioDispatcher) {
            prefs.setElements(items)
            router.navigateBack()
        }

    fun toggleVisibility(
        element: Element,
        isVisible: Boolean,
    ) {
        items.first { it.element == element }.apply {
            this.isVisible = isVisible
        }
    }
}