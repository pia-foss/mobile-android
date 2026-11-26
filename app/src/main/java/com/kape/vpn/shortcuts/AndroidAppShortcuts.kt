package com.kape.vpn.shortcuts

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.vpn.MainActivity
import com.kape.vpn.R
import org.koin.core.annotation.Singleton
import com.kape.ui.R as UiR

@Singleton(binds = [AppShortcuts::class])
internal class AndroidAppShortcuts(
    private val context: Context,
) : AppShortcuts {
    override fun publish(isConnected: Boolean) {
        val connectLabel = if (isConnected) UiR.string.qs_disconnect_nolocation else UiR.string.qs_title
        val connectAction = if (isConnected) ShortcutAction.Disconnect else ShortcutAction.Connect
        val shortcuts =
            listOf(
                shortcut(CONNECT_ID, connectLabel, R.drawable.ic_protected, connectAction),
                shortcut(CHANGE_SERVER_ID, UiR.string.change_server, R.drawable.ic_drawer_region, ShortcutAction.ServerSelection),
                shortcut(SETTINGS_ID, UiR.string.settings, UiR.drawable.ic_settings, ShortcutAction.Settings),
            )
        try {
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        } catch (_: IllegalStateException) {
            // Android rate-limits shortcut updates. The next update brings them back in line.
        }
    }

    private fun shortcut(
        id: String,
        @StringRes label: Int,
        @DrawableRes icon: Int,
        action: ShortcutAction,
    ): ShortcutInfoCompat =
        ShortcutInfoCompat
            .Builder(context, id)
            .setShortLabel(context.getString(label))
            .setLongLabel(context.getString(label))
            .setIcon(IconCompat.createWithResource(context, icon))
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    this.action = action.intentAction
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            ).build()

    private companion object {
        // Kept from mobile-android, so shortcuts users pinned keep working after updating.
        const val CONNECT_ID = "connecttovpn"
        const val CHANGE_SERVER_ID = "changeServer"
        const val SETTINGS_ID = "settings"
    }
}

@Singleton(binds = [ShortcutRequestRepository::class])
internal class PrefsShortcutRequestRepository(
    private val prefs: ShortcutPrefs,
) : ShortcutRequestRepository {
    override suspend fun record(action: ShortcutAction) =
        when (action) {
            ShortcutAction.Connect -> prefs.setShortcutConnectToVpn(true)
            ShortcutAction.Disconnect -> prefs.setShortcutDisconnectVpn(true)
            ShortcutAction.ServerSelection -> prefs.setShortcutChangeServer(true)
            ShortcutAction.Settings -> prefs.setShortcutSettings(true)
        }
}