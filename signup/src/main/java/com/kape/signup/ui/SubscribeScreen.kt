package com.kape.signup.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.kape.signup.ui.vm.SubscribeViewModel
import com.kape.signup.utils.Plan
import com.kape.uicomponents.components.ButtonProperties
import com.kape.uicomponents.components.PrimaryButton
import com.kape.uicomponents.components.SecondaryButton
import com.kape.uicomponents.components.UiResources
import com.kape.uicomponents.theme.*
import org.koin.androidx.compose.viewModel

@Composable
fun SubscriptionScreen() {

    val viewModel: SubscribeViewModel by viewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    val subscribeProperties =
        ButtonProperties(label = stringResource(id = R.string.subscribe_now).toUpperCase(Locale.current), enabled = true, onClick = {
        })

    val loginProperties =
        ButtonProperties(label = stringResource(id = R.string.login).toUpperCase(Locale.current), enabled = true, onClick = {
        })

    LaunchedEffect(key1 = Unit) {
        viewModel.loadPrices()
    }

    state.data?.let {
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
                    text = stringResource(id = R.string.screen_title),
                    fontSize = FontSize.Title,
                    modifier = Modifier
                        .align(CenterHorizontally)
                )
                Text(
                    text = stringResource(id = R.string.screen_description).format(it.yearly.mainPrice),
                    fontSize = FontSize.Normal, modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(horizontal = Space.BIG, vertical = Space.SMALL),
                    textAlign = TextAlign.Center, color = Grey55
                )
                Spacer(modifier = Modifier.height(Space.NORMAL))
                PriceRow(state = it.yearly, it.selected.value == it.yearly, it.selected)
                Spacer(modifier = Modifier.height(Space.SMALL))
                PriceRow(state = it.monthly, it.selected.value == it.monthly, it.selected)
                Spacer(modifier = Modifier.height(Space.MEDIUM))
                PrimaryButton(modifier = Modifier.padding(Space.MEDIUM, Space.MINI), properties = subscribeProperties)
                SecondaryButton(modifier = Modifier.padding(Space.MEDIUM, Space.MINI), properties = loginProperties)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(id = R.string.footer), fontSize = FontSize.Normal, color = Grey55, modifier = Modifier
                        .padding(Space.NORMAL)
                        .align(CenterHorizontally)
                )
            }
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
        border = if (selected) BorderStroke(1.dp, DarkGreen20) else BorderStroke(1.dp, Grey55),
        backgroundColor = if (selected) Color.White else Color.Transparent,
        elevation = 0.dp
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
                Text(text = state.period, fontSize = FontSize.Normal, color = Grey55)
                Spacer(modifier = Modifier.height(Space.MINI))
                Text(text = state.mainPrice, fontSize = FontSize.Title, color = Grey20)
                if (state.bestValue) {
                    Spacer(modifier = Modifier.height(Space.SMALL))
                    Card(backgroundColor = ConnectingOrange, elevation = 0.dp) {
                        Text(
                            text = stringResource(id = R.string.best_value),
                            fontSize = FontSize.Small,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = Space.MINI, vertical = Space.SMALL_VERTICAL)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(Space.NORMAL))
            state.secondaryPrice?.let {
                Text(
                    text = state.secondaryPrice,
                    fontSize = FontSize.Normal,
                    color = Grey55,
                    modifier = Modifier
                        .align(CenterVertically)
                )
            }
        }
    }
}