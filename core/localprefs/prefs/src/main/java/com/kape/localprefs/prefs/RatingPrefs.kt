package com.kape.localprefs.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kape.localprefs.Prefs
import com.kape.rating.data.RatingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

private val RATING_STATE = stringPreferencesKey("rating-state")

@Singleton
class RatingPrefs(
    context: Context,
) : Prefs(context, "rating") {
    fun setRatingState(state: RatingState) {
        scope.launch {
            dataStore.edit { it[RATING_STATE] = Json.encodeToString(state) }
        }
    }

    fun getRatingState(): Flow<RatingState> =
        dataStore.data.map { prefs ->
            prefs[RATING_STATE]?.let { Json.decodeFromString(it) } ?: RatingState(true, 0)
        }
}