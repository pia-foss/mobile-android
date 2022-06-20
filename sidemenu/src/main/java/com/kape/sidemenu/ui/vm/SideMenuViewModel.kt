package com.kape.sidemenu.ui.vm

import androidx.lifecycle.ViewModel
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

class SideMenuViewModel : ViewModel() {
    val isLoggedIn: Boolean = Random.nextBoolean()
    val username: String = if (isLoggedIn) "P4713533" else ""

    val versionName: String = "3.15.0"
    val versionCode: String = "585"

    val showExpiryItem: Boolean = if (isLoggedIn) Random.nextBoolean() else false
    val subscriptionExpiryTime: LocalDateTime = when {
        showExpiryItem -> LocalDateTime.now()
                .plusDays(Random.nextLong(until = 9) - 4)
                .plusHours(Random.nextLong(until = 24))
                .plusMinutes(Random.nextLong(until = 60))
        else -> LocalDateTime.now()
    }

    internal fun compatToHoursPart(duration: Duration): Int {
        return (duration.toHours() % 24).toInt()
    }

    internal fun compatToMinutesPart(duration: Duration): Int {
        return (duration.toMinutes() % 60).toInt()
    }
}
