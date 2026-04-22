package com.kape.localprefs.prefs

import android.content.Context
import com.kape.customization.data.Element
import com.kape.localprefs.Prefs
import com.kape.localprefs.data.customization.ScreenElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private const val ORDERED_ELEMENTS = "ordered-elements"
private const val ORDERED_ELEMENTS_V2 = "ordered-elements-v2"

@Singleton
class CustomizationPrefs(
    context: Context,
) : Prefs(context, "customization") {
    @Deprecated("Deprecated in favor of getElements()")
    fun getOrderedElements(): List<ScreenElement> =
        prefs.getString(ORDERED_ELEMENTS, null)?.let {
            Json.decodeFromString(it)
        } ?: defaultList()

    @Deprecated("Deprecated in favor of setElements()")
    fun setOrderedElements(orderedElements: List<ScreenElement>) =
        prefs
            .edit()
            .putString(
                ORDERED_ELEMENTS,
                Json.encodeToString(orderedElements),
            ).apply()

    fun getElements(): List<ScreenElement> =
        prefs.getString(ORDERED_ELEMENTS_V2, null)?.let {
            Json.decodeFromString(it)
        } ?: run {
            if (getOrderedElements().isNotEmpty()) {
                getOrderedElements()
            } else {
                defaultList()
            }
        }

    fun setElements(orderedElements: List<ScreenElement>) =
        prefs
            .edit()
            .putString(
                ORDERED_ELEMENTS_V2,
                Json.encodeToString(orderedElements),
            ).apply()

    private fun defaultList(): List<ScreenElement> =
        listOf(
            ScreenElement(Element.VpnRegionSelection, "vpn-region-selection"),
            ScreenElement(
                Element.ShadowsocksRegionSelection,
                "shadowsocks-region-selection",
                shouldDisplayElement = false,
            ),
            ScreenElement(Element.IpInfo, "ip-info"),
            ScreenElement(Element.QuickConnect, "quick-connect"),
            ScreenElement(Element.QuickSettings, name = "quick-settings"),
            ScreenElement(Element.Snooze, name = "snooze"),
            ScreenElement(Element.Traffic, isVisible = false, name = "traffic"),
            ScreenElement(Element.ConnectionInfo, isVisible = false, name = "conection-info"),
        )
}