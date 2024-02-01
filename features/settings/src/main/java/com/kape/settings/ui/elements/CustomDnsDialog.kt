package com.kape.settings.ui.elements

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
    CustomDnsInputFieldDialog(
        R.string.network_dns_selection_title,
        current = customDns,
        onConfirm = {
            val bothDnsEmpty = it.primaryDns.isEmpty() && it.secondaryDns.isEmpty()
            val primaryDnsOK = it.primaryDns.isNotEmpty() && isDnsNumeric(it.primaryDns)
            val secondaryDnsOK = it.secondaryDns.isEmpty() || isDnsNumeric(it.secondaryDns)

            if (bothDnsEmpty || (primaryDnsOK && secondaryDnsOK)) {
                onConfirm(it)
            } else {
                return@CustomDnsInputFieldDialog
            }
        },
        onDismiss = onDismiss,
        footnote,
        isDnsNumeric,
    )
}