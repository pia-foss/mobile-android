package com.kape.about.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.about.domain.GetLicencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AboutViewModel(
    getLicences: GetLicencesUseCase,
) : ViewModel() {
    private val _licences = MutableStateFlow<List<String>>(emptyList())
    val licences: StateFlow<List<String>> = _licences.asStateFlow()

    init {
        viewModelScope.launch { _licences.value = getLicences() }
    }
}