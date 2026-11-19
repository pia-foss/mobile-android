package com.kape.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun formatDate(epochMillis: Long): String {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    return remember(epochMillis, locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(epochMillis)) }
}