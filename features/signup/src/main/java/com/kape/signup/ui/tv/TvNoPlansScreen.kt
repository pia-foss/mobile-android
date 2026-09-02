package com.kape.signup.ui.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.elements.SecondaryButton
import com.kape.ui.tv.text.OnboardingDescriptionText
import com.kape.ui.tv.text.OnboardingTitleText
import com.kape.ui.tv.text.SignUpTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvNoPlansScreen() =
    Screen {
        val viewModel: SignupViewModel = koinViewModel()

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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_large),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .width(100.dp)
                                .height(40.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SignUpTitleText(
                        content = stringResource(id = R.string.signup),
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                    Card(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = LocalColors.current.onPrimaryContainer,
                            ),
                    ) {
                        Image(
                            painter = painterResource(id = com.kape.signup.R.drawable.ic_tv_signup),
                            contentScale = ContentScale.Fit,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 16.dp),
                        )
                    }
                }
            }
            VerticalDivider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 64.dp)
                        .width(0.5.dp),
                color = LocalColors.current.primaryContainer,
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 64.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                OnboardingTitleText(
                    content = stringResource(id = R.string.subscribe_screen_title),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OnboardingDescriptionText(
                    content = stringResource(id = R.string.subscribe_screen_description_no_in_app),
                )
                Spacer(modifier = Modifier.height(32.dp))
                SecondaryButton(
                    text = stringResource(id = R.string.subscribe_screen_log_in),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    viewModel.navigateToTvLogin()
                }
            }
        }
    }