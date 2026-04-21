package com.kape.rating.utils

import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.data.DI
import com.kape.localprefs.prefs.RatingPrefs
import com.kape.rating.data.RatingDialogType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private const val INITIAL_RATING_COUNTER = 3
private const val RATING_REMINDER_COUNTER = 50
private const val REVIEW_REMINDER_WAIT_DAYS = 30
private const val DATE_FORMAT = "dd/MM/yyyy"

class RatingTool(
    private val connectionStatusProvider: ConnectionStatusProvider,
    private val prefs: RatingPrefs,
    @Named(DI.MAIN_DISPATCHER) private val mainDispatcher: CoroutineDispatcher,
) {
    // Because the state events are being reported multiple time per connection cycle, we need
    // to make sure a connected is counted with its disconnection accordingly.
    private var shouldCountConnectedEvent = true
    private val job = Job()

    private val _showRating: MutableStateFlow<RatingDialogType?> = MutableStateFlow(null)
    val showRating: StateFlow<RatingDialogType?> = _showRating

    fun start() {
        if (prefs.getRatingState().active) {
            job.start()

            CoroutineScope(mainDispatcher).launch {
                connectionStatusProvider.state.collectLatest {
                    when (it.status) {
                        ConnectionStatus.CONNECTED -> {
                            if (prefs.getRatingState().active && shouldCountConnectedEvent) {
                                shouldCountConnectedEvent = true
                                handleConnectedEvent(prefs)
                            }
                        }

                        ConnectionStatus.DISCONNECTED -> {
                            shouldCountConnectedEvent = true
                        }

                        ConnectionStatus.CONNECTING,
                        ConnectionStatus.DISCONNECTING,
                        ConnectionStatus.ERROR,
                        ConnectionStatus.RECONNECTING,
                        -> {
                            // no-op
                        }
                    }
                }
            }
        } else {
            job.cancel()
        }
    }

    fun setRatingInactive() {
        val updatedState = prefs.getRatingState().copy(active = false)
        prefs.setRatingState(updatedState)
        _showRating.value = null
    }

    fun updateRatingDate() {
        val dateFormat = SimpleDateFormat(DATE_FORMAT)
        val dateString = dateFormat.format(Date())
        val updatedState = prefs.getRatingState().copy(notEnjoyingDate = dateString)
        prefs.setRatingState(updatedState)
        _showRating.value = null
    }

    private fun handleConnectedEvent(prefs: RatingPrefs) {
        val knownState = prefs.getRatingState()
        val updatedState = knownState.copy(counter = knownState.counter + 1)
        prefs.setRatingState(updatedState)

        if (updatedState.counter == INITIAL_RATING_COUNTER && updatedState.notEnjoyingDate == null) {
            _showRating.value = RatingDialogType.General
        } else if (
            updatedState.counter >= RATING_REMINDER_COUNTER &&
            updatedState.notEnjoyingDate != null &&
            daysPassedSinceNotEnjoyingReply(updatedState.notEnjoyingDate) >= REVIEW_REMINDER_WAIT_DAYS
        ) {
            _showRating.value = RatingDialogType.Review
        }
    }

    private fun daysPassedSinceNotEnjoyingReply(dateString: String?): Long {
        return dateString?.let {
            val dateFormat = SimpleDateFormat(DATE_FORMAT)
            val date = dateFormat.parse(dateString)
            return TimeUnit.DAYS.convert(Date().time - date.time, TimeUnit.MILLISECONDS)
        } ?: 0
    }
}