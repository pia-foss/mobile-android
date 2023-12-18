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
        ScreenElement(Element.RegionSelection, true, 0),
        ScreenElement(Element.IpInfo, true, 1),
        ScreenElement(Element.QuickConnect, true, 2),
        ScreenElement(Element.QuickSettings, true, 3),
        ScreenElement(Element.Snooze, true, 4),
        ScreenElement(Element.Traffic, true, 5),
        ScreenElement(Element.ConnectionInfo, true, 6),
    )
}