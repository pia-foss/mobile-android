package com.kape.signup.utils

import androidx.compose.runtime.MutableState

data class SubscribeScreenState(
    val idle: Boolean,
    val loading: Boolean,
    val data: SubscriptionData? = null,
    val destination: String? = null
)

data class SubscriptionData(val selected: MutableState<Plan>, val yearly: Plan, val monthly: Plan)

data class Plan(val id: String, val period: String, val bestValue: Boolean, val mainPrice: String, val secondaryPrice: String? = null)

val IDLE = SubscribeScreenState(idle = true, loading = false)
val LOADING = SubscribeScreenState(idle = false, loading = true)
fun loaded(data: SubscriptionData) = SubscribeScreenState(idle = false, loading = false, data = data)
fun navigate(destination: String) = SubscribeScreenState(idle = false, loading = false, destination = destination)