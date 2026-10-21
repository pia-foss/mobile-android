package com.kape.settings.ui.screens.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.settings.ui.elements.MockLocationSetupEffect
import com.kape.settings.ui.elements.openDeveloperSettings
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.OnboardingDescriptionText
import com.kape.ui.tv.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvMockLocationSettingScreen() =
    Screen {
        val viewModel: SettingsViewModel = koinViewModel()
        val context = LocalContext.current
        val initialFocusRequester = remember { FocusRequester() }

        MockLocationSetupEffect(viewModel)

        LaunchedEffect(key1 = Unit) {
            initialFocusRequester.requestFocus()
        }

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(LocalColors.current.background),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(64.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .semantics(mergeDescendants = true) { },
                ) {
                    OnboardingTitleText(
                        content = stringResource(id = R.string.mock_location_setup_title),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OnboardingDescriptionText(
                        content = stringResource(id = R.string.mock_location_setup_description),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                PrimaryButton(
                    text = stringResource(id = R.string.mock_location_open_developer_options),
                    modifier =
                        Modifier
                            .focusRequester(initialFocusRequester)
                            .padding(16.dp),
                ) {
                    openDeveloperSettings(context)
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tv_settings),
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                )
            }
        }
    }