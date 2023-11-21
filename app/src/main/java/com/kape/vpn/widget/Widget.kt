package com.kape.vpn.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: to be implemented by https://polymoon.atlassian.net/browse/PIA-930
        provideContent {
            Text(
                text = "sample widget",
            )
        }
    }
}