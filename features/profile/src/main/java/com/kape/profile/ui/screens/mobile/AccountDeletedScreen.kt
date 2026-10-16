package com.kape.profile.ui.screens.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.profile.ui.vm.ProfileViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingTitleText
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountDeletedScreen() {
    val viewModel: ProfileViewModel = koinViewModel()

    AdaptiveSplitContent(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
        header = { isCompact ->
            Image(
                painter = painterResource(id = R.drawable.pia_medium),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .height(40.dp)
                        .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(if (isCompact) 32.dp else 8.dp))
            Image(
                painter = painterResource(id = R.drawable.img_success),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .size(if (isCompact) 150.dp else 120.dp),
            )
            if (isCompact) {
                Spacer(modifier = Modifier.height(32.dp))
            }
        },
        body = {
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                OnboardingTitleText(
                    content = stringResource(id = R.string.account_deletion_confirmation_title),
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(32.dp))
                OnboardingDescriptionText(
                    content = stringResource(id = R.string.account_deletion_confirmation_message),
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                text = stringResource(id = R.string.login),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            ) {
                viewModel.navigateToLogin()
            }
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = stringResource(id = R.string.subscribe),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            ) {
                viewModel.navigateToSubscribe()
            }
            Spacer(modifier = Modifier.height(16.dp))
        },
    )
}