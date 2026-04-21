package com.kape.data

const val AUTO_KEY = "auto"
const val NOTIFICATION_ID = 123
const val STATUS_REQUEST_LONG_TIMEOUT = 2000L
const val NO_IP = "---"
const val QUICK_CONNECT_MAX_SERVERS = 5

object SnoozeIntervals {
    const val FIVE_MINUTES = 5 * 60 * 1000
    const val FIFTEEN_MINUTES = 15 * 60 * 1000
    const val SIXTY_MINUTES = 60 * 60 * 1000
}

object Dns {
    const val MACE = "10.0.0.241"
    const val PIA = "10.0.0.243"
}

object DI {
    const val RULES_UPDATED_INTENT = "rules-updated-intent"
    const val RULES_UPDATED_BROADCAST = "rules-updated-broadcast"
    const val VPN_LAUNCHER = "vpn-launcher"
    const val AUTOMATION_PENDING_INTENT = "automation-pending-intent"
    const val AUTOMATION_SERVICE_INTENT = "automation-service-intent"
    const val UPDATE_URL = "update-url"
    const val WIDGET_PENDING_INTENT = "widget-pending-intent"
    const val MAIN_DISPATCHER = "main-dispatcher"
    const val IO_DISPATCHER = "io-dispatcher"
}

object WorkerTags {
    const val SNOOZE_WORKER = "snooze-worker"
    const val PORT_FORWARDING_WORKER = "port-forwarding-worker"
}