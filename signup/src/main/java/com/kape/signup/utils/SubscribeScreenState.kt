package com.kape.signup.utils

import androidx.compose.runtime.MutableState

data class SubscribeScreenState(val idle: Boolean, val loading: Boolean, val data: SubscriptionData? = null)

data class SubscriptionData(val selected: MutableState<Plan>, val yearly: Plan, val monthly: Plan)

data class Plan(val id: String, val period: String, val bestValue: Boolean, val mainPrice: String, val secondaryPrice: String? = null)