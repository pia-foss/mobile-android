package com.kape.localprefs.prefs

import android.content.Context
import com.kape.localprefs.Prefs
import com.kape.rating.data.RatingState
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private const val RATING_STATE = "rating-state"

@Singleton
class RatingPrefs(
    context: Context,
) : Prefs(context, "rating") {
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