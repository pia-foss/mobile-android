package com.kape.signup.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kape.signup.R
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.SubscriptionData
import com.kape.ui.elements.HtmlText
import com.kape.ui.elements.MonthlySubscriptionCard
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.elements.SecondaryButton
import com.kape.ui.elements.YearlySubscriptionCard
import com.kape.ui.text.OnboardingDescriptionText
import com.kape.ui.text.OnboardingTitleText
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.utils.LocalColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(viewModel: SignupViewModel, subscriptionData: SubscriptionData?) = Screen {
    val scheme = LocalColors.current
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(scheme.statusBarDefault(scheme))
    }

    BackHandler {
        viewModel.exitApp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background)
            .semantics {
                testTagsAsResourceId = true
            },
    ) {
        Image(
            painter = painterResource(id = R.drawable.map),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = com.kape.ui.R.drawable.pia_medium),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .height(40.dp)
                    .fillMaxWidth(),
            )
            Image(
                painter = painterResource(id = R.drawable.ic_globe),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .size(150.dp),
            )
            OnboardingTitleText(
                content = stringResource(id = R.string.subscribe_screen_title),
                modifier = Modifier
                    .align(CenterHorizontally),
            )
            OnboardingDescriptionText(
                content = stringResource(id = R.string.subscribe_screen_description).format(
                    subscriptionData?.yearly?.mainPrice,
                ),
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            YearlySubscriptionCard(
                selected = subscriptionData?.selected?.value == subscriptionData?.yearly,
                price = subscriptionData?.yearly?.mainPrice ?: "",
                perMonthPrice = subscriptionData?.yearly?.secondaryPrice ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                subscriptionData?.let {
                    subscriptionData.selected.value = subscriptionData.yearly
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            MonthlySubscriptionCard(
                selected = subscriptionData?.selected?.value == subscriptionData?.monthly,
                price = subscriptionData?.monthly?.mainPrice ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                subscriptionData?.let {
                    subscriptionData.selected.value = subscriptionData.monthly
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(id = R.string.subscribe_now),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                subscriptionData?.let {
                    viewModel.purchase(subscriptionData.selected.value.id)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = stringResource(id = R.string.login),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(":SignUpScreen:Login"),
            ) {
                viewModel.navigateToLogin()
            }
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.weight(1f))
            HtmlText(
                textId = R.string.footer,
                modifier = Modifier
                    .padding(16.dp)
                    .align(CenterHorizontally),
            )
        }
    }
}