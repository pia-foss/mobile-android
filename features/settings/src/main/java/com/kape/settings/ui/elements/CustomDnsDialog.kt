package com.kape.settings.ui.elements

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kape.settings.R
import com.kape.settings.data.CustomDns

@Composable
fun CustomDnsDialog(
    customDns: CustomDns,
    displayFootnote: Boolean,
    onConfirm: (custom: CustomDns) -> Unit,
    onDismiss: () -> Unit,
    isDnsNumeric: (ipAddress: String) -> Boolean,
) {
    val footnote =
        if (displayFootnote) stringResource(id = R.string.custom_dns_disabling_mace) else null
    InputFieldDialog(
        R.string.network_dns_selection_title,
        current = customDns,
        onConfirm = {
            if (
                (it.primaryDns.isNotEmpty() && isDnsNumeric(it.primaryDns).not()) ||
                (it.secondaryDns.isNotEmpty() && isDnsNumeric(it.secondaryDns).not())
            ) {
                Log.e("aaa", "onConfirm: should return: $it")
                return@InputFieldDialog
            } else {
                onConfirm(it)
            }
        },
        onDismiss = onDismiss,
        footnote,
        isDnsNumeric,
    )
}