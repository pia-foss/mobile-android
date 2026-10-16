package com.kape.splash.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.splash.ui.vm.SplashViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.OnboardingDescriptionPaymentText
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingTitleText
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdateScreen(viewModel: SplashViewModel = koinViewModel()) =
    Screen {
        val context = LocalContext.current

        AdaptiveSplitContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
            header = { isCompact ->
                if (isCompact) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.shield),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .padding(if (isCompact) 40.dp else 16.dp)
                            .height(if (isCompact) 200.dp else 120.dp)
                            .fillMaxWidth(),
                )
            },
            body = {
                Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                    OnboardingTitleText(
                        content = stringResource(id = R.string.update_required_title),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    )

                    OnboardingDescriptionText(
                        content = stringResource(id = R.string.update_required_message),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    )
                }
                // Pins the action to the bottom, below the message rather than over it
                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = stringResource(id = R.string.update_required_action),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp, bottom = 36.dp, end = 16.dp),
                ) {
                    viewModel.onUpdateClicked(
                        launchUpdate = { url ->
                            val launchIntent = Intent(Intent.ACTION_VIEW)
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            launchIntent.data = Uri.parse(url)
                            // Silently fail if Google Play Store isn't installed.
                            if (launchIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(launchIntent)
                            }
                        },
                    )
                }

                if (viewModel.isConnected()) {
                    OnboardingDescriptionPaymentText(stringResource(R.string.update_required_info))

                    Spacer(modifier = Modifier.padding(bottom = 40.dp))
                }
            },
        )
    }