package com.kape.dedicatedip.ui.screens.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.dedicatedip.utils.DedicatedIpStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun DedicatedIpFlow(initialStep: DedicatedIpStep) {
    val viewModel: DipViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    // If the viewmodel doesn't have a known state. Use the initial one.
    val step = if (state == null) {
        initialStep
    } else {
        state
    }

    step?.let {
        when (it) {
            DedicatedIpStep.ActivateToken -> DedicatedIpScreen()
            DedicatedIpStep.SignupPlans -> SignupDedicatedIpScreen()
            DedicatedIpStep.LocationSelection -> SignupDedicatedIpCountryScreen()
            DedicatedIpStep.SignupSuccess -> SignupDedicatedIpPurchaseSuccessScreen()
            DedicatedIpStep.SignupTokenDetails -> SignupDedicatedIpTokenDetailsScreen()
            DedicatedIpStep.SignupTokenActivate -> SignupDedicatedIpTokenActivateScreen()
        }
    }
}