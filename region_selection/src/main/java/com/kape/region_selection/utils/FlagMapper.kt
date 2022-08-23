package com.kape.region_selection.utils

import android.content.Context
import com.kape.region_selection.R
import java.util.*

fun getFlagResource(context: Context, serverIso: String): Int {
    val resName = String.format(
        Locale.US,
        "flag_%s",
        serverIso.replace(" ", "_").replace(",", "").lowercase()
    )
    var flagResource: Int =
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    if (flagResource == 0) {
        flagResource = R.drawable.flag_world
    }
    return flagResource
}