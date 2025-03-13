package com.kape.signup.ui.mobile

import android.app.Activity
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.NO_IN_APP_SUBSCRIPTIONS
import com.kape.signup.utils.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.utils.SignupScreenState
import com.kape.signup.utils.SubscriptionData
import com.kape.ui.R
import com.kape.ui.mobile.elements.Footer
import com.kape.ui.mobile.elements.MonthlySubscriptionCard
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.elements.YearlySubscriptionCard
import com.kape.ui.mobile.text.OnboardingDescriptionPaymentText
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingTitleText
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.utils.LocalColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(viewModel: SignupViewModel, subscriptionData: SubscriptionData?) = Screen {
    val screenState by viewModel.state.collectAsState()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.registerClientIfNeeded(context as Activity)
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
            painter = painterResource(id = com.kape.signup.R.drawable.map),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.pia_medium),
                    contentDescription = stringResource(id = R.string.pia_signup),
                    modifier = Modifier
                        .padding(16.dp)
                        .height(40.dp)
                        .fillMaxWidth(),
                )
                Image(
                    painter = painterResource(id = com.kape.signup.R.drawable.ic_globe),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .size(150.dp),
                )
                Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                    OnboardingTitleText(
                        content = stringResource(id = R.string.subscribe_screen_title),
                        modifier = Modifier
                            .align(CenterHorizontally),
                    )
                    OnboardingDescriptionText(
                        content = if (screenState == NO_IN_APP_SUBSCRIPTIONS || screenState == SUBSCRIPTIONS_FAILED_TO_LOAD) {
                            stringResource(id = R.string.subscribe_screen_description_no_in_app)
                        } else {
                            "${
                            stringResource(id = R.string.subscribe_screen_description).format(
                                subscriptionData?.yearly?.mainPrice,
                            )
                            } ${stringResource(id = R.string.subscribe_screen_description_cancel_anytime)}"
                        },
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.alpha(determineProductsAlpha(screenState))) {
                    val subscriptionOptions = stringResource(id = R.string.subscription_option)
                    YearlySubscriptionCard(
                        selected = subscriptionData?.selected?.value == subscriptionData?.yearly,
                        price = subscriptionData?.yearly?.mainPrice ?: "",
                        perMonthPrice = subscriptionData?.yearly?.secondaryPrice ?: "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .semantics { contentDescription = subscriptionOptions },
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
                            .padding(horizontal = 16.dp)
                            .semantics { contentDescription = subscriptionOptions },
                    ) {
                        subscriptionData?.let {
                            subscriptionData.selected.value = subscriptionData.monthly
                        }
                    }
                }
                if (screenState == NO_IN_APP_SUBSCRIPTIONS || screenState == SUBSCRIPTIONS_FAILED_TO_LOAD) {
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    OnboardingDescriptionPaymentText(
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .padding(horizontal = 20.dp),
                        content = stringResource(id = R.string.subscribe_screen_description_payment),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                PrimaryButton(
                    text = stringResource(id = R.string.subscribe_now),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .alpha(determineSubscribeButtonAlpha(screenState)),
                ) {
                    subscriptionData?.let {
                        viewModel.purchase(subscriptionData.selected.value.id)
                    } ?: run {
                        viewModel.navigateToWebsite()
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
                Footer(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(CenterHorizontally),
                )
            }
        }
    }
}

private fun determineProductsAlpha(state: SignupScreenState): Float {
    return if (state == NO_IN_APP_SUBSCRIPTIONS || state == SUBSCRIPTIONS_FAILED_TO_LOAD) {
        0f
    } else {
        1f
    }
}

private fun determineSubscribeButtonAlpha(state: SignupScreenState): Float {
    return if (state == SUBSCRIPTIONS_FAILED_TO_LOAD) {
        0f
    } else {
        1f
    }
}