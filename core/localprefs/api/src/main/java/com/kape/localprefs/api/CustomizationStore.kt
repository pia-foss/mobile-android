package com.kape.localprefs.api

import com.kape.localprefs.data.customization.ScreenElement
import kotlinx.coroutines.flow.StateFlow

interface CustomizationStore {
    val elements: StateFlow<List<ScreenElement>>

    suspend fun setElements(orderedElements: List<ScreenElement>)
}