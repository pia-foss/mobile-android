package com.kape.connection.domain.usecase

import com.kape.localprefs.api.ShortcutStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.core.annotation.Factory

enum class ShortcutRequest { Connect, Disconnect, ChangeServer, OpenSettings }

@Factory
class ObserveShortcutRequestsUseCase(
    private val shortcutPrefs: ShortcutStore,
) {
    operator fun invoke(): Flow<ShortcutRequest> =
        merge(
            shortcutPrefs.isShortcutConnectToVpn.whenSet(ShortcutRequest.Connect) { shortcutPrefs.setShortcutConnectToVpn(false) },
            shortcutPrefs.isShortcutDisconnectVpn.whenSet(ShortcutRequest.Disconnect) { shortcutPrefs.setShortcutDisconnectVpn(false) },
            shortcutPrefs.isShortcutChangeServer.whenSet(ShortcutRequest.ChangeServer) { shortcutPrefs.setShortcutChangeServer(false) },
            shortcutPrefs.isShortcutSettings.whenSet(ShortcutRequest.OpenSettings) { shortcutPrefs.setShortcutSettings(false) },
        )

    private fun Flow<Boolean>.whenSet(
        request: ShortcutRequest,
        markHandled: suspend () -> Unit,
    ): Flow<ShortcutRequest> =
        filter { it }.map {
            markHandled()
            request
        }
}