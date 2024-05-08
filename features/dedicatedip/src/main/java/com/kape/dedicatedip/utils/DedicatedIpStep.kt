package com.kape.dedicatedip.utils

sealed class DedicatedIpStep {
    data object ActivateToken : DedicatedIpStep()
    data object SignupPlans : DedicatedIpStep()
}