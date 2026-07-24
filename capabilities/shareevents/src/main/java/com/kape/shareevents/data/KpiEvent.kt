package com.kape.shareevents.data

import com.kape.data.kpi.KpiEventPropertyKey
import com.privateinternetaccess.kpi.KPIClientEvent
import kotlinx.datetime.Clock

private const val IAP_PROCESSING_PURCHASE = "iap_processing_purchase"
private const val IAP_PROCESSING_SUCCESS = "iap_processing_success"
private const val IAP_PROCESSING_FAILURE = "iap_processing_failure"
private const val IAP_PROCESSING_RETRY = "iap_processing_retry"

private const val ORIGIN = "signup"
private const val ENVIRONMENT = "production"

fun processingPurchaseEvent(): KPIClientEvent =
    KPIClientEvent(
        eventName = IAP_PROCESSING_PURCHASE,
        eventProperties =
            mapOf(
                KpiEventPropertyKey.Origin.value to ORIGIN,
                KpiEventPropertyKey.Environment.value to ENVIRONMENT,
            ),
        eventInstant = Clock.System.now(),
    )

fun processingSuccess(): KPIClientEvent =
    KPIClientEvent(
        eventName = IAP_PROCESSING_SUCCESS,
        eventProperties =
            mapOf(
                KpiEventPropertyKey.Origin.value to ORIGIN,
                KpiEventPropertyKey.Environment.value to ENVIRONMENT,
            ),
        eventInstant = Clock.System.now(),
    )

fun processingFailure(error: String): KPIClientEvent =
    KPIClientEvent(
        eventName = IAP_PROCESSING_FAILURE,
        eventProperties =
            mapOf(
                KpiEventPropertyKey.Origin.value to ORIGIN,
                KpiEventPropertyKey.Environment.value to ENVIRONMENT,
                KpiEventPropertyKey.Error.value to error,
            ),
        eventInstant = Clock.System.now(),
    )