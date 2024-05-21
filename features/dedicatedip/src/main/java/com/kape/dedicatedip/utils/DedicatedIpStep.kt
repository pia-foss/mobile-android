package com.kape.dedicatedip.utils

sealed class DedicatedIpStep {
    data object ActivateToken : DedicatedIpStep()
    data object SignupPlans : DedicatedIpStep()
    data object LocationSelection : DedicatedIpStep()
    data object SignupSuccess : DedicatedIpStep()
    data object SignupTokenDetails : DedicatedIpStep()
}