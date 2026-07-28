package com.kape.signup.ui.mobile

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.kape.signup.ui.shared.CheckmarkText
import com.kape.signup.ui.shared.SubscriptionDescriptionText
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.NO_IN_APP_SUBSCRIPTIONS
import com.kape.signup.utils.SUBSCRIPTIONS_FAILED_TO_LOAD
import com.kape.signup.utils.SubscriptionData
import com.kape.ui.R
import com.kape.ui.mobile.elements.Footer
import com.kape.ui.mobile.elements.MonthlySubscriptionCard
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.elements.YearlySubscriptionCard
import com.kape.ui.theme.PiaTypography
import com.kape.ui.utils.LocalColors

@Composable
fun NewPaywallSignUpScreen(
    viewModel: SignupViewModel,
    subscriptionData: SubscriptionData?,
) {
    val screenState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current

    BackHandler {
        activity?.finish()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.registerClientIfNeeded(context as Activity)
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(com.kape.signup.R.drawable.background),
                    alignment = Alignment.TopEnd,
                    alpha = 0.2f,
                )
                .semantics {
                    testTagsAsResourceId = true
                },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .padding(horizontal = 20.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = CenterHorizontally,
        ) {
            if (subscriptionData == null ||
                screenState == NO_IN_APP_SUBSCRIPTIONS ||
                screenState == SUBSCRIPTIONS_FAILED_TO_LOAD
            ) {
                Spacer(Modifier.weight(1f))
                CommonContent()
                Spacer(Modifier.height(16.dp))
                NoPlansContent(
                    onNavigateToLogin = {
                        viewModel.navigateToLogin()
                    },
                )
            } else {
                CommonContent()
                Spacer(modifier = Modifier.height(24.dp))
                PlansPresentContent(
                    subscriptionData = subscriptionData,
                    onPurchase = {
                        viewModel.purchase(it, context as Activity)
                    },
                    onNavigateToLogin = {
                        viewModel.navigateToLogin()
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Footer(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .align(CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommonContent() {
    Image(
        painter = painterResource(id = com.kape.signup.R.drawable.pia_full_name),
        contentDescription = stringResource(id = R.string.pia_signup),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(4) {
            Image(
                painter = painterResource(R.drawable.rating_star_full),
                contentDescription = null,
            )
        }
        Image(
            painter = painterResource(R.drawable.rating_star_half),
            contentDescription = null,
        )
        Spacer(Modifier.widthIn(8.dp))
        Text(
            text = stringResource(R.string.subscribe_screen_ratings_label),
            style = PiaTypography.caption1,
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Image(
        painter = painterResource(id = com.kape.signup.R.drawable.globe_with_padlock),
        contentDescription = null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.PlansPresentContent(
    subscriptionData: SubscriptionData,
    onPurchase: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val bottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    var showBottomSheet by remember { mutableStateOf(false) }

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
    Spacer(modifier = Modifier.height(24.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = LocalColors.current.surfaceVariant),
    ) {
        ConstraintLayout(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            val (image, title, today, day7) = createRefs()

            Image(
                painter = painterResource(com.kape.signup.R.drawable.timeline),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .constrainAs(image) {
                            start.linkTo(parent.start)
                            top.linkTo(title.bottom)
                            bottom.linkTo(parent.bottom)
                        },
            )
            Text(
                text = stringResource(R.string.subscribe_screen_trial_title),
                style = PiaTypography.subtitle2,
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                        .constrainAs(title) {
                            start.linkTo(image.end)
                            top.linkTo(parent.top)
                        },
            )
            Column(
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .fillMaxHeight()
                        .constrainAs(today) {
                            start.linkTo(image.end)
                            top.linkTo(image.top)
                        },
            ) {
                Text(
                    text = stringResource(R.string.subscribe_screen_trial_today),
                    style = PiaTypography.subtitle3,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.subscribe_screen_trial_today_description),
                    style = PiaTypography.body3,
                )
            }
            Column(
                modifier =
                    Modifier
                        .padding(start = 16.dp, bottom = 4.dp)
                        .fillMaxHeight()
                        .constrainAs(day7) {
                            start.linkTo(image.end)
                            bottom.linkTo(image.bottom)
                        },
            ) {
                Text(
                    text = stringResource(R.string.subscribe_screen_trial_day_7),
                    style = PiaTypography.subtitle3,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.subscribe_screen_trial_day_7_description),
                    style = PiaTypography.body3,
                )
            }
        }
    }

    Spacer(Modifier.weight(1f))
    SubscriptionDescriptionText(
        subscriptionData = subscriptionData,
        selectedPlan = subscriptionData.yearly,
    )
    Spacer(Modifier.height(20.dp))
    PrimaryButton(
        text = stringResource(id = R.string.subscribe_screen_trial_start_button),
        modifier = Modifier.fillMaxWidth(),
    ) {
        onPurchase(subscriptionData.yearly.id)
    }
    Spacer(modifier = Modifier.height(12.dp))
    SecondaryButton(
        text = stringResource(id = R.string.subscribe_screen_trial_other_plans_button),
        modifier =
            Modifier.fillMaxWidth(),
    ) {
        showBottomSheet = true
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(id = R.string.subscribe_screen_log_in).uppercase(),
        style = PiaTypography.body3,
        textAlign = TextAlign.Center,
        color = LocalColors.current.primary,
        modifier =
            Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
                .clickable {
                    onNavigateToLogin()
                }
                .testTag(":SignUpScreen:Login"),
    )

    if (showBottomSheet) {
        val selectedPlan = subscriptionData.selected.value

        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            containerColor = LocalColors.current.surfaceVariant,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.subscribe_screen_choose_your_plan),
                    style = PiaTypography.h2,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                val subscriptionOptions =
                    stringResource(id = R.string.subscription_option)
                YearlySubscriptionCard(
                    selected = selectedPlan == subscriptionData.yearly,
                    price = subscriptionData.yearly.mainPrice,
                    additionalText = stringResource(R.string.subscribe_screen_billed_annually),
                    selectedCardColor = Color.Transparent,
                    bestValueBannerText = stringResource(R.string.subscribe_screen_best_value),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = subscriptionOptions },
                ) {
                    subscriptionData.selected.value = subscriptionData.yearly
                }
                Spacer(modifier = Modifier.height(16.dp))
                MonthlySubscriptionCard(
                    selected = selectedPlan == subscriptionData.monthly,
                    price = subscriptionData.monthly.mainPrice,
                    additionalText = stringResource(R.string.subscribe_screen_billed_monthly),
                    selectedCardColor = Color.Transparent,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = subscriptionOptions },
                ) {
                    subscriptionData.selected.value = subscriptionData.monthly
                }
                Spacer(Modifier.height(20.dp))
                SubscriptionDescriptionText(
                    subscriptionData = subscriptionData,
                    selectedPlan = selectedPlan,
                )
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text =
                        if (selectedPlan.hasFreeTrial) {
                            stringResource(id = R.string.subscribe_screen_trial_start_button)
                        } else {
                            "${stringResource(id = R.string.subscribe)} • ${selectedPlan.mainPrice}"
                        },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    onPurchase(selectedPlan.id)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.subscribe_screen_maybe_later).uppercase(),
                    style = PiaTypography.body3,
                    textAlign = TextAlign.Center,
                    color = LocalColors.current.primary,
                    modifier =
                        Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                            },
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ColumnScope.NoPlansContent(onNavigateToLogin: () -> Unit) {
    Text(
        text = stringResource(id = R.string.subscribe_screen_title),
        style = PiaTypography.h2,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(id = R.string.subscribe_screen_description_no_in_app),
        style = PiaTypography.body1,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.weight(1f))
    SecondaryButton(
        text = stringResource(id = R.string.subscribe_screen_log_in),
        modifier =
            Modifier.fillMaxWidth(),
        onClick = onNavigateToLogin,
    )
}
