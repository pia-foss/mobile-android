package com.kape.utils

const val FIVE_MINUTES = 5 * 60 * 1000
const val FIFTEEN_MINUTES = 15 * 60 * 1000
const val SIXTY_MINUTES = 60 * 60 * 1000
const val AUTO_KEY = "auto"

object DI {
    const val RULES_UPDATED_INTENT = "rules-updated-intent"
    const val RULES_UPDATED_BROADCAST = "rules-updated-broadcast"
    const val VPN_LAUNCHER = "vpn-launcher"
    const val AUTOMATION_PENDING_INTENT = "automation-pending-intent"
    const val AUTOMATION_SERVICE_INTENT = "automation-service-intent"
    const val PORT_FORWARDING_RECEIVER_INTENT = "port-forwarding-receiver-intent"
    const val PORT_FORWARDING_PENDING_INTENT = "port-forwarding-pending-intent"
    const val SET_SNOOZE_PENDING_INTENT = "set-snooze-pending-intent"
    const val CANCEL_SNOOZE_PENDING_INTENT = "cancel-snooze-pending-intent"
    const val ALARM_MANAGER = "alarm-manager"
    const val UPDATE_URL = "update-url"
    const val WIDGET_PENDING_INTENT = "widget-pending-intent"
}