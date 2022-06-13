package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.profile.domain.GetProfileUseCase

class ProfileViewModel(private val getProfile: GetProfileUseCase) : ViewModel() {
}