package com.kape.tvwelcome.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.Router
import com.kape.data.TvLoginUsername
import com.kape.data.TvSubscribe
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TvWelcomeViewModel(
    private val router: Router,
    buildConfigProvider: BuildConfigProvider,
) : ViewModel() {
    val shouldShowSubscribeButton = buildConfigProvider.isGoogleFlavor()

    fun login() = router.updateDestination(TvLoginUsername)

    fun signup() = router.updateDestination(TvSubscribe)
}