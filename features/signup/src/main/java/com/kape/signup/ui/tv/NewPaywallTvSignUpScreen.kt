package com.kape.signup.ui.tv

import android.app.Activity
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.signup.ui.shared.CheckmarkText
import com.kape.signup.ui.shared.SubscriptionDescriptionText
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.sunglow
import com.kape.ui.tv.elements.MonthlySubscriptionCard
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.elements.TertiaryButton
import com.kape.ui.tv.elements.YearlySubscriptionCard
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun NewPaywallTvSignUpScreen() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val subscriptionData = state.subscriptionData
    val initialFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current

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
                    .padding(horizontal = 48.dp, vertical = 64.dp),
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
                Image(
                    painter = painterResource(id = com.kape.signup.R.drawable.globe_with_padlock),
                    contentDescription = null,
                )
                CheckmarkText(
                    stringResource(id = R.string.subscribe_screen_unrestricted_access),
                )
                Spacer(modifier = Modifier.height(8.dp))
                CheckmarkText(
                    stringResource(id = R.string.subscribe_screen_connection_speeds),
                )
                Spacer(modifier = Modifier.height(8.dp))
                CheckmarkText(
                    stringResource(id = R.string.subscribe_screen_unlimited_devices),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = LocalColors.current.onPrimaryContainer,
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.subscribe_screen_trial_title),
                            style =
                                PiaTypography.subtitle3.copy(
                                    lineHeight = 19.sp,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .weight(0.33f)
                                    .wrapContentHeight(Alignment.CenterVertically),
                        )
                        Image(
                            painter = painterResource(com.kape.signup.R.drawable.timeline),
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier =
                                Modifier
                                    .weight(0.66f)
                                    .fillMaxHeight(),
                        ) {
                            Text(
                                text = stringResource(R.string.subscribe_screen_trial_today),
                                style = PiaTypography.subtitle3,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.subscribe_screen_trial_today_description),
                                style = PiaTypography.caption2,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text =
                                    stringResource(
                                        R.string.subscribe_screen_trial_day_to_format,
                                        viewModel.isoDurationToDays(subscriptionData?.yearly?.freeTrialDuration)
                                            ?: 0,
                                    ),
                                style = PiaTypography.subtitle3,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.subscribe_screen_trial_day_7_description),
                                style = PiaTypography.caption2,
                            )
                        }
                    }
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
                    .padding(horizontal = 24.dp, vertical = 64.dp),
        ) {
            Text(
                text =
                    buildAnnotatedString {
                        val price =
                            subscriptionData?.yearly?.mainPrice?.let {
                                stringResource(
                                    R.string.yearly_ending,
                                    it,
                                )
                            } ?: ""
                        val template =
                            stringResource(
                                id = R.string.tv_subscribe_screen_description_to_format,
                                viewModel.isoDurationToDays(subscriptionData?.yearly?.freeTrialDuration)
                                    ?: 0,
                                price,
                            )
                        val placeholderIndex = if (price.isEmpty()) -1 else template.indexOf(price)
                        if (placeholderIndex >= 0) {
                            append(template.substring(0, placeholderIndex))
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    color = LocalColors.current.sunglow(),
                                ),
                            ) {
                                append(price)
                            }
                            append(template.substring(placeholderIndex + price.length))
                        } else {
                            append(template)
                        }
                    },
                color = LocalColors.current.onSurface,
                style = PiaTypography.subtitle1,
            )
            Spacer(modifier = Modifier.weight(1f))
            YearlySubscriptionCard(
                selected = subscriptionData?.selected?.value == subscriptionData?.yearly,
                price =
                    stringResource(
                        R.string.yearly_ending,
                        subscriptionData?.yearly?.mainPrice ?: "",
                    ),
                perMonthPrice = subscriptionData?.yearly?.secondaryPrice ?: "",
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .focusRequester(initialFocusRequester),
                freeTrialDays = viewModel.isoDurationToDays(subscriptionData?.yearly?.freeTrialDuration),
            ) {
                subscriptionData?.let {
                    subscriptionData.selected.value = subscriptionData.yearly
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            MonthlySubscriptionCard(
                selected = subscriptionData?.selected?.value == subscriptionData?.monthly,
                price =
                    subscriptionData?.monthly?.mainPrice?.let {
                        "$it ${stringResource(R.string.subscribe_screen_per_month_ending)}"
                    } ?: "",
                freeTrialDays = viewModel.isoDurationToDays(subscriptionData?.monthly?.freeTrialDuration),
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
            ) {
                subscriptionData?.let {
                    subscriptionData.selected.value = subscriptionData.monthly
                }
            }
            subscriptionData?.let {
                Spacer(modifier = Modifier.weight(1f))
                SubscriptionDescriptionText(
                    subscriptionData = it,
                    selectedPlan = it.selected.value,
                    convertToDays = viewModel::isoDurationToDays,
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text =
                        if (it.selected.value.hasFreeTrial) {
                            stringResource(
                                id = R.string.subscribe_screen_trial_start_button_to_format,
                                viewModel.isoDurationToDays(it.selected.value.freeTrialDuration)
                                    ?: 0,
                            )
                        } else {
                            "${stringResource(id = R.string.subscribe)} • ${it.selected.value.mainPrice}"
                        },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = LocalColors.current.primary,
                    contentColor = LocalColors.current.onPrimary,
                    uppercase = false,
                ) {
                    viewModel.purchase(
                        it.selected.value.id,
                        context as Activity,
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TertiaryButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.drawer_item_title_privacy_policy),
                ) {
                    viewModel.navigateToPrivacyPolicy()
                }
                Spacer(modifier = Modifier.width(16.dp))
                TertiaryButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.terms_of_service),
                ) {
                    viewModel.navigateToTermsOfService()
                }
            }
        }
    }
}