package com.kape.dedicatedip.ui.screens.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.DedicatedIpSignupDescriptionText
import com.kape.ui.mobile.text.DedicatedIpSignupTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupDedicatedIpPurchaseSuccessScreen() = Screen {
    val viewModel: DipViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dip),
            tint = LocalColors.current.primary,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .height(40.dp)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.img_success),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        DedicatedIpSignupTitleText(
            content = stringResource(id = R.string.dip_signup_purchase_success_title),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        DedicatedIpSignupDescriptionText(
            content = stringResource(id = R.string.dip_signup_purchase_success_description),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.weight(1.0f))
        PrimaryButton(
            text = stringResource(id = R.string.dip_signup_generate_your_token),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            viewModel.navigateToDedicatedIpTokenDetails()
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}