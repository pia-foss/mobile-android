package com.kape.tvwelcome.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.router.Router
import com.kape.router.Subscribe
import com.kape.router.TvLoginUsername
import com.kape.router.TvSubscribe
import org.koin.core.component.KoinComponent

class TvWelcomeViewModel(
    private val router: Router,
    buildConfigProvider: BuildConfigProvider,
) : ViewModel(), KoinComponent {

    val shouldShowSubscribeButton = buildConfigProvider.isGoogleFlavor()

    fun login() = router.updateDestination(TvLoginUsername)

    fun signup() = router.updateDestination(TvSubscribe)
}