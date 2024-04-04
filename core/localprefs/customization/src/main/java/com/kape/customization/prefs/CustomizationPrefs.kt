package com.kape.customization.prefs

import android.content.Context
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.utils.Prefs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val ORDERED_ELEMENTS = "ordered-elements"

class CustomizationPrefs(context: Context) : Prefs(context, "customization") {

    fun getOrderedElements(): List<ScreenElement> = prefs.getString(ORDERED_ELEMENTS, null)?.let {
        Json.decodeFromString(it)
    } ?: defaultList()

    fun setOrderedElements(orderedElements: List<ScreenElement>) = prefs.edit().putString(
        ORDERED_ELEMENTS,
        Json.encodeToString(orderedElements),
    ).apply()

    private fun defaultList(): List<ScreenElement> = listOf(
        ScreenElement(Element.VpnRegionSelection, "vpn-region-selection"),
        ScreenElement(Element.ShadowsocksRegionSelection, "shadowsocks-region-selection"),
        ScreenElement(Element.IpInfo, "ip-info"),
        ScreenElement(Element.QuickConnect, "quick-connect"),
        ScreenElement(Element.QuickSettings, name = "quick-settings"),
//        ScreenElement(Element.Snooze),
        ScreenElement(Element.Traffic, isVisible = false, name = "traffic"),
        ScreenElement(Element.ConnectionInfo, isVisible = false, name = "conection-info"),
    )
}