package com.kape.vpn.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.kape.ui.utils.getFlagResource
import com.kape.vpn.R
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class CommandCenterWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val connectionManager: ConnectionManager by inject()
    private val serviceIntent: PendingIntent by inject(named("service-intent"))
    private val usageProvider: UsageProvider by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let {
                val views: RemoteViews = RemoteViews(
                    context.packageName,
                    R.layout.widget_command_center,
                ).apply {
                    when (connectionManager.connectionStatus.value) {
                        ConnectionStatus.CONNECTED -> {
                            setImageViewResource(
                                R.id.widget_image,
                                getFlagResource(context, connectionManager.serverIso.value),
                            )
                            setViewVisibility(R.id.widget_progress, View.GONE)
                            setViewVisibility(R.id.widget_image, View.VISIBLE)
                            setViewVisibility(R.id.widget_pia_logo, View.VISIBLE)
                            setTextViewText(
                                R.id.widget_top_text,
                                connectionManager.serverName.value,
                            )
                            setTextViewText(
                                R.id.widget_speed_down,
                                "${
                                    usageProvider.widgetDownloadSpeed.value
                                } / ${usageProvider.widgetDownload.value}",
                            )
                            setTextViewText(
                                R.id.widget_speed_up,
                                "${
                                    usageProvider.widgetUploadSpeed.value
                                } / ${usageProvider.widgetUpload.value}",
                            )
                        }

                        ConnectionStatus.RECONNECTING,
                        ConnectionStatus.CONNECTING,
                        -> {
                            setViewVisibility(R.id.widget_progress, View.VISIBLE)
                            setViewVisibility(R.id.widget_image, View.GONE)
                            setViewVisibility(R.id.widget_pia_logo, View.GONE)
                            setTextViewText(
                                R.id.widget_top_text,
                                context.getText(R.string.connecting),
                            )
                        }

                        ConnectionStatus.DISCONNECTED -> {
                            setImageViewResource(
                                R.id.widget_image,
                                com.kape.ui.R.drawable.flag_world,
                            )
                            setViewVisibility(R.id.widget_progress, View.GONE)
                            setViewVisibility(R.id.widget_image, View.VISIBLE)
                            setViewVisibility(R.id.widget_pia_logo, View.VISIBLE)
                            setTextViewText(
                                R.id.widget_top_text,
                                context.getText(R.string.tap_to_connect),
                            )
                            usageProvider.reset()
                            setTextViewText(
                                R.id.widget_speed_down,
                                "${
                                    usageProvider.widgetDownloadSpeed.value
                                } / ${usageProvider.widgetDownload.value}",
                            )
                            setTextViewText(
                                R.id.widget_speed_up,
                                "${
                                    usageProvider.widgetUploadSpeed.value
                                } / ${usageProvider.widgetUpload.value}",
                            )
                        }
                    }
                    setOnClickPendingIntent(R.id.widget, serviceIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}