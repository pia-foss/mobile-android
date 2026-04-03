package com.kape.vpn.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.kape.ui.R
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_CONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_DISCONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SERVER_SELECTION
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SETTINGS
import com.kape.utils.DI
import com.kape.vpn.MainActivity
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnconnect.utils.ConnectionStatusProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

@Singleton
class ShortcutManager(
    private val context: Context,
    private val connectionStatusProvider: ConnectionStatusProvider,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
) {
    init {
        CoroutineScope(ioDispatcher).launch {
            connectionStatusProvider.state.collect {
                when (it) {
                    ConnectionStatus.CONNECTED,
                    ConnectionStatus.DISCONNECTED,
                        -> withContext(Dispatchers.Main) {
                        createDynamicShortcuts()
                    }

                    else -> {
                        // no-op
                    }
                }
            }
        }
    }

    fun createDynamicShortcuts() {
        val isConnected = connectionStatusProvider.state.value == ConnectionStatus.CONNECTED
        val connect = ShortcutInfoCompat.Builder(context, CONNECT)
            .setShortLabel(context.getString(if (isConnected) R.string.qs_disconnect_nolocation else R.string.qs_title))
            .setLongLabel(context.getString(if (isConnected) R.string.qs_disconnect_nolocation else R.string.qs_title))
            .setIcon(IconCompat.createWithResource(context, com.kape.vpn.R.drawable.ic_protected))
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    action = if (isConnected) ACTION_DISCONNECT else ACTION_CONNECT
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            .build()

        val servers = ShortcutInfoCompat.Builder(context, CHANGE_SERVER)
            .setShortLabel(context.getString(R.string.change_server))
            .setLongLabel(context.getString(R.string.change_server))
            .setIcon(
                IconCompat.createWithResource(
                    context,
                    com.kape.sidemenu.R.drawable.ic_drawer_region,
                ),
            )
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    action = ACTION_SERVER_SELECTION
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            .build()

        val settings = ShortcutInfoCompat.Builder(context, SETTINGS)
            .setShortLabel(context.getString(R.string.settings))
            .setLongLabel(context.getString(R.string.settings))
            .setIcon(IconCompat.createWithResource(context, com.kape.ui.R.drawable.ic_settings))
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    action = ACTION_SETTINGS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            .build()

        ShortcutManagerCompat.setDynamicShortcuts(context, listOf(connect, servers, settings))
    }

    companion object {
        private const val CONNECT = "connecttovpn"
        private const val CHANGE_SERVER = "changeServer"
        private const val SETTINGS = "settings"
    }
}