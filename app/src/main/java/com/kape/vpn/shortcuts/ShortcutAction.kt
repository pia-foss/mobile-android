package com.kape.vpn.shortcuts

import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_CONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_DISCONNECT
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SERVER_SELECTION
import com.kape.ui.utils.ExternallyUsed.Constants.ACTION_SETTINGS

enum class ShortcutAction(
    val intentAction: String,
) {
    Connect(ACTION_CONNECT),
    Disconnect(ACTION_DISCONNECT),
    ServerSelection(ACTION_SERVER_SELECTION),
    Settings(ACTION_SETTINGS),
    ;

    companion object {
        fun fromIntentAction(action: String?): ShortcutAction? = entries.firstOrNull { it.intentAction == action }
    }
}