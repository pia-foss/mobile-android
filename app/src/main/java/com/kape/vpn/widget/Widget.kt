package com.kape.vpn.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: to be implemented by https://polymoon.atlassian.net/browse/PIA-930
        provideContent {
            GlanceTheme(colors = WidgetColors.colors) {
                Column(
                    modifier = GlanceModifier.background(GlanceTheme.colors.background)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "sample widget",
                    )
                }
            }
        }
    }
}