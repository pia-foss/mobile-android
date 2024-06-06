package com.kape.rating.data

import kotlinx.serialization.Serializable

@Serializable
data class RatingState(val active: Boolean, val counter: Int, val notEnjoyingDate: String? = null)