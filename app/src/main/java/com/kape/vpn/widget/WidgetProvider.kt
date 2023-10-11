package com.kape.vpn.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.kape.ui.utils.getFlagResource
import com.kape.vpn.R
import com.kape.vpn.service.WidgetProviderService
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WidgetProvider : AppWidgetProvider(), KoinComponent {

    private val connectionManager: ConnectionManager by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val pendingIntent: PendingIntent = PendingIntent.getService(
                context,
                0,
                Intent(context, WidgetProviderService::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let {
                val views: RemoteViews = RemoteViews(
                    context.packageName,
                    it.initialLayout,
                ).apply {
                    when (connectionManager.connectionStatus.value) {
                        ConnectionStatus.CONNECTED -> {
                            setImageViewResource(
                                R.id.image,
                                getFlagResource(context, connectionManager.serverIso.value),
                            )
                            setViewVisibility(R.id.widget_progress, View.GONE)
                            setViewVisibility(R.id.image, View.VISIBLE)
                            setViewVisibility(R.id.small_image, View.VISIBLE)
                        }

                        ConnectionStatus.RECONNECTING,
                        ConnectionStatus.CONNECTING,
                        -> {
                            setViewVisibility(R.id.widget_progress, View.VISIBLE)
                            setViewVisibility(R.id.image, View.GONE)
                            setViewVisibility(R.id.small_image, View.GONE)
                        }

                        ConnectionStatus.DISCONNECTED -> {
                            setImageViewResource(
                                R.id.image,
                                com.kape.ui.R.drawable.flag_world,
                            )
                            setViewVisibility(R.id.widget_progress, View.GONE)
                            setViewVisibility(R.id.image, View.VISIBLE)
                            setViewVisibility(R.id.small_image, View.VISIBLE)
                        }
                    }
                    setOnClickPendingIntent(R.id.widget, pendingIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}