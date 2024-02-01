package com.kape.settings.ui.elements

import androidx.compose.runtime.Composable
import com.kape.settings.R
import com.kape.settings.data.CustomObfuscation

@Composable
fun CustomObfuscationDialog(
    customObfuscation: CustomObfuscation?,
    onConfirm: (custom: CustomObfuscation) -> Unit,
    onDismiss: () -> Unit,
    isNumericIpAddress: (ipAddress: String) -> Boolean,
    isPortValid: (port: String) -> Boolean,
) {
    CustomObfuscationInputFieldDialog(
        R.string.obfuscation_shadowsocks_title,
        current = customObfuscation,
        onConfirm = {
            if (it.isValid() && isNumericIpAddress(it.host) && isPortValid(it.port)) {
                onConfirm(it)
            } else {
                return@CustomObfuscationInputFieldDialog
            }
        },
        onDismiss = onDismiss,
        isNumericIpAddress = isNumericIpAddress,
        isPortValid = isPortValid,
    )
}