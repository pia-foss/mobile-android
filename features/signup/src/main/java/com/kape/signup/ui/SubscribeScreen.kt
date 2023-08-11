package com.kape.signup.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.kape.signup.R
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.Plan
import com.kape.signup.utils.SubscriptionData
import com.kape.ui.elements.ButtonProperties
import com.kape.ui.elements.HtmlText
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.SecondaryButton
import com.kape.ui.elements.UiResources
import com.kape.ui.theme.ConnectingOrange
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.utils.LocalColors

@Composable
fun SubscriptionScreen(viewModel: SignupViewModel, subscriptionData: SubscriptionData) {

    val subscribeProperties =
        ButtonProperties(
            label = stringResource(id = R.string.subscribe_now).toUpperCase(Locale.current),
            enabled = true,
            onClick = {
                viewModel.purchase(subscriptionData.selected.value.id)
            })

    val loginProperties =
        ButtonProperties(
            label = stringResource(id = R.string.login).toUpperCase(Locale.current),
            enabled = true,
            onClick = {
                viewModel.navigateToLogin()
            })

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = UiResources.bigAppLogo),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier
                    .height(Height.BIG_LOGO)
                    .fillMaxWidth()
                    .padding(Space.NORMAL)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_globe),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier
                    .padding(Space.NORMAL)
                    .fillMaxWidth()
                    .size(Square.GLOBE)
            )
            Text(
                text = stringResource(id = R.string.subscribe_screen_title),
                fontSize = FontSize.Title,
                modifier = Modifier
                    .align(CenterHorizontally)
            )
            Text(
                text = stringResource(id = R.string.subscribe_screen_description).format(
                    subscriptionData.yearly.mainPrice
                ),
                fontSize = FontSize.Normal, modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(horizontal = Space.BIG, vertical = Space.SMALL),
                textAlign = TextAlign.Center, color = LocalColors.current.outline
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            PriceRow(
                state = subscriptionData.yearly,
                subscriptionData.selected.value == subscriptionData.yearly,
                subscriptionData.selected
            )
            Spacer(modifier = Modifier.height(Space.SMALL))
            PriceRow(
                state = subscriptionData.monthly,
                subscriptionData.selected.value == subscriptionData.monthly,
                subscriptionData.selected
            )
            Spacer(modifier = Modifier.height(Space.MEDIUM))
            PrimaryButton(
                modifier = Modifier.padding(Space.MEDIUM, Space.MINI),
                properties = subscribeProperties
            )
            SecondaryButton(
                modifier = Modifier.padding(Space.MEDIUM, Space.MINI),
                properties = loginProperties
            )
            Spacer(modifier = Modifier.weight(1f))
            HtmlText(
                textId = R.string.footer, modifier = Modifier
                    .padding(Space.NORMAL)
                    .align(CenterHorizontally)
            )
        }
    }
}

@Composable
fun PriceRow(state: Plan, selected: Boolean, selectedState: MutableState<Plan>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(Height.PLAN)
            .padding(horizontal = Space.MEDIUM)
            .clickable {
                selectedState.value = state
            },
        border = if (selected) BorderStroke(
            1.dp,
            LocalColors.current.primary
        ) else BorderStroke(1.dp, LocalColors.current.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LocalColors.current.surface else Color.Transparent
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.NORMAL)
        ) {
            Icon(
                painter = if (selected) painterResource(id = R.drawable.ic_selection_checked) else painterResource(
                    id = R.drawable.ic_selection_default
                ),
                contentDescription = stringResource(id = R.string.checkbox),
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(Square.PLAN)
                    .align(CenterVertically)
            )
            Spacer(modifier = Modifier.width(Space.NORMAL))
            Column(modifier = Modifier.align(CenterVertically)) {
                Text(
                    text = state.period,
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.outlineVariant
                )
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(
                    text = state.mainPrice,
                    fontSize = FontSize.Title,
                    color = LocalColors.current.onSurface
                )
                if (state.bestValue) {
                    Spacer(modifier = Modifier.height(Space.SMALL))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ConnectingOrange
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.best_value),
                            fontSize = FontSize.Small,
                            color = Color.Black,
                            modifier = Modifier.padding(
                                horizontal = Space.MINI,
                                vertical = Space.SMALL_VERTICAL
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(Space.NORMAL))
            state.secondaryPrice?.let {
                Text(
                    text = state.secondaryPrice,
                    fontSize = FontSize.Normal,
                    color = LocalColors.current.outlineVariant,
                    modifier = Modifier
                        .align(CenterVertically)
                )
            }
        }
    }
}