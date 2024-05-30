package com.kape.dip.data

import kotlinx.serialization.Serializable

@Serializable
data class DedicatedIpSignupPlans(
    val persistedTimestamp: Long,
    val signupPlans: FetchedDedicatedIpSignupPlansMock,
)