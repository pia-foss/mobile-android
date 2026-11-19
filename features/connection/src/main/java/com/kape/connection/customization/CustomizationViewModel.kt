package com.kape.connection.customization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.domain.usecase.ObserveScreenElementsUseCase
import com.kape.contracts.Router
import com.kape.customization.data.Element
import com.kape.localprefs.api.CustomizationStore
import com.kape.localprefs.data.customization.ScreenElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CustomizationViewModel(
    observeScreenElements: ObserveScreenElementsUseCase,
    private val customizationPrefs: CustomizationStore,
    private val router: Router,
) : ViewModel() {
    private val _items = MutableStateFlow<List<ScreenElement>>(emptyList())
    val items: StateFlow<List<ScreenElement>> = _items.asStateFlow()

    init {
        viewModelScope.launch { _items.value = observeScreenElements().first().customization }
    }

    fun onMove(
        fromIndex: Int,
        toIndex: Int,
    ) {
        _items.value = _items.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    fun toggleVisibility(element: Element) {
        _items.value = _items.value.map { if (it.element == element) it.copy(isVisible = !it.isVisible) else it }
    }

    /** Saving first, as closing the screen cancels this view model's work. */
    fun save() {
        viewModelScope.launch {
            customizationPrefs.setElements(_items.value)
            router.navigateBack()
        }
    }
}