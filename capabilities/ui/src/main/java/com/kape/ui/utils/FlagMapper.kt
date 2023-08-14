package com.kape.ui.utils

import android.content.Context
import com.kape.ui.R
import java.util.Locale

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