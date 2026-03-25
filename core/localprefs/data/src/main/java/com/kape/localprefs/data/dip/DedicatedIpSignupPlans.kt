package com.kape.dip.data

import com.privateinternetaccess.account.model.response.AndroidAddonsSubscriptionsInformation
import kotlinx.serialization.Serializable

@Serializable
data class DedicatedIpSignupPlans(
    val persistedTimestamp: Long,
    val signupPlans: AndroidAddonsSubscriptionsInformation,
)