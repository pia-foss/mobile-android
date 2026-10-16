package com.kape.dedicatedip.ui.screens.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.DedicatedIpSignupDescriptionText
import com.kape.ui.mobile.text.DedicatedIpSignupTitleText
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupDedicatedIpPurchaseSuccessScreen() =
    Screen {
        val viewModel: DipViewModel = koinViewModel()

        AdaptiveSplitContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                    .padding(8.dp),
            header = { isCompact ->
                DipSignupIcon()
                Image(
                    painter = painterResource(id = R.drawable.img_success),
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                    // Kept smaller beside the text, where height is scarce
                    modifier =
                        if (isCompact) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillMaxWidth().heightIn(max = 160.dp)
                        },
                )
                Spacer(modifier = Modifier.height(16.dp))
            },
            body = {
                DedicatedIpSignupTitleText(
                    content = stringResource(id = R.string.dip_signup_purchase_success_title),
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                DedicatedIpSignupDescriptionText(
                    content = stringResource(id = R.string.dip_signup_purchase_success_description),
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.weight(1.0f))
                PrimaryButton(
                    text = stringResource(id = R.string.logjn_continue),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                ) {
                    viewModel.navigateToDedicatedIpTokenDetails()
                }
                Spacer(modifier = Modifier.height(64.dp))
            },
        )
    }