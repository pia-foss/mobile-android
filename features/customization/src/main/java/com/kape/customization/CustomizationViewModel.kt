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
    private var originalItems = emptyList<ScreenElement>()

    init {
        refreshElements()
    }

    fun onMove(
        from: LazyListItemInfo,
        to: LazyListItemInfo,
    ) {
        items.add(to.index, items.removeAt(from.index))
    }

    fun saveOrder() {
        val snapshot = items.map { it.copy() }
        originalItems = snapshot
        viewModelScope.launch(ioDispatcher) {
            prefs.setElements(snapshot)
            router.navigateBack()
        }
    }

    fun toggleVisibility(
        element: Element,
        isVisible: Boolean,
    ) {
        items.first { it.element == element }.apply {
            this.isVisible = isVisible
        }
    }

    fun discardChanges() {
        items.clear()
        items.addAll(originalItems.map { it.copy() })
    }

    private fun refreshElements() {
        viewModelScope.launch {
            screenElementProvider.customizationElements.collectLatest {
                originalItems = it.map { element -> element.copy() }
                items.clear()
                items.addAll(it.map { element -> element.copy() })
            }
        }
    }
}