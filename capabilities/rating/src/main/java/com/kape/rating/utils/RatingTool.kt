package com.kape.rating.utils

import com.kape.contracts.ConnectionStatusSource
import com.kape.contracts.di.Qualifiers
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.rating.data.RatingDialogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val INITIAL_RATING_COUNTER = 3
private const val RATING_REMINDER_COUNTER = 50
private const val REVIEW_REMINDER_WAIT_DAYS = 30
private const val DATE_FORMAT = "dd/MM/yyyy"

@Singleton
class RatingTool(
    connectionStatus: ConnectionStatusSource,
    private val prefs: RatingPrefs,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
) {
    private val _showRating = MutableStateFlow<RatingDialogType?>(null)
    val showRating: StateFlow<RatingDialogType?> = _showRating.asStateFlow()

    init {
        appScope.launch {
            connectionStatus.status.collect { status ->
                if (status == ConnectionStatus.CONNECTED) onConnected()
            }
        }
    }

    suspend fun setRatingInactive() {
        prefs.setRatingState(prefs.getRatingStateNow().copy(active = false))
        _showRating.value = null
    }

    suspend fun updateRatingDate() {
        val today = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        prefs.setRatingState(prefs.getRatingStateNow().copy(notEnjoyingDate = today))
        _showRating.value = null
    }

    private suspend fun onConnected() {
        val known = prefs.getRatingStateNow()
        if (!known.active) return
        val updated = known.copy(counter = known.counter + 1)
        prefs.setRatingState(updated)
        _showRating.value = ratingPromptFor(updated.counter, updated.notEnjoyingDate?.let(::daysSince))
    }

    private fun daysSince(date: String): Long? =
        SimpleDateFormat(DATE_FORMAT, Locale.US).parse(date)?.let { TimeUnit.DAYS.convert(Date().time - it.time, TimeUnit.MILLISECONDS) }
}

internal fun ratingPromptFor(
    connections: Int,
    daysSinceNotEnjoying: Long?,
): RatingDialogType? =
    when {
        connections == INITIAL_RATING_COUNTER && daysSinceNotEnjoying == null -> RatingDialogType.General
        connections >= RATING_REMINDER_COUNTER && daysSinceNotEnjoying != null && daysSinceNotEnjoying >= REVIEW_REMINDER_WAIT_DAYS ->
            RatingDialogType.Review
        else -> null
    }