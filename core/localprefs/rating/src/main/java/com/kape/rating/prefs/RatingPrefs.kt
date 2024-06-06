package com.kape.rating.prefs

import android.content.Context
import com.kape.rating.data.RatingState
import com.kape.utils.Prefs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val RATING_STATE = "rating-state"

class RatingPrefs(context: Context) : Prefs(context, "rating") {

    fun setRatingState(state: RatingState) {
        prefs.edit().putString(RATING_STATE, Json.encodeToString(state)).apply()
    }

    fun getRatingState(): RatingState {
        prefs.getString(RATING_STATE, null)?.let {
            return Json.decodeFromString(it)
        } ?: run {
            return RatingState(true, 0)
        }
    }
}