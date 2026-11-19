package com.kape.vpn.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.login.domain.usecase.LoginWithLinkUseCase
import com.kape.vpn.automation.StartAutomationIfEnabledUseCase
import com.kape.vpn.shortcuts.RecordShortcutActionUseCase
import com.kape.vpn.shortcuts.ShortcutAction
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val router: Router,
    private val loginWithLink: LoginWithLinkUseCase,
    private val recordShortcutAction: RecordShortcutActionUseCase,
    startAutomationIfEnabled: StartAutomationIfEnabledUseCase,
) : ViewModel() {
    init {
        viewModelScope.launch { startAutomationIfEnabled() }
    }

    fun onIntent(
        action: String?,
        data: String?,
    ) {
        ShortcutAction.fromIntentAction(action)?.let { shortcut ->
            viewModelScope.launch { recordShortcutAction(shortcut) }
        }
        data?.let { url ->
            viewModelScope.launch { loginWithLink(url)?.let(router::updateDestination) }
        }
    }
}