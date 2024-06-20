package com.kape.dedicatedip.ui.screens.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Footer
import com.kape.ui.mobile.elements.MonthlySubscriptionCard
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.elements.YearlySubscriptionCard
import com.kape.ui.mobile.text.DedicatedIpSignupDescriptionText
import com.kape.ui.mobile.text.DedicatedIpSignupTitleText
import com.kape.ui.mobile.text.SupportedDipRegions
import com.kape.ui.mobile.text.SupportedDipRegionsInCountry
import com.kape.ui.theme.connectionError
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.privateinternetaccess.account.model.response.DipCountriesResponse
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupDedicatedIpScreen() = Screen {
    val viewModel: DipViewModel = koinViewModel()

    LaunchedEffect(key1 = Unit) {
        viewModel.hasActivePlaystoreSubscription()
        viewModel.getDipSupportedCountries()
        viewModel.getDipMonthlyPlan()
        viewModel.getDipYearlyPlan()
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dip),
            tint = LocalColors.current.primary,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .height(40.dp)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (viewModel.showFetchingPlansSpinner.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }
        } else {
            Column {
                DedicatedIpSignupTitleText(
                    content = stringResource(id = R.string.dip_signup_addon_title),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                DedicatedIpSignupDescriptionText(
                    content = stringResource(id = R.string.dip_signup_addon_description),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp),
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            YearlySubscriptionCard(
                selected = viewModel.selectedPlanProductId.value == viewModel.dipYearlyPlan.value?.id,
                price = viewModel.dipYearlyPlan.value?.yearlyPrice.toString(),
                perMonthPrice = viewModel.dipYearlyPlan.value?.monthlyPrice.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                viewModel.dipYearlyPlan.value?.let {
                    viewModel.selectPlanProductId(it.id)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            MonthlySubscriptionCard(
                selected = viewModel.selectedPlanProductId.value == viewModel.dipMonthlyPlan.value?.id,
                price = viewModel.dipMonthlyPlan.value?.monthlyPrice.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                viewModel.dipMonthlyPlan.value?.let {
                    viewModel.selectPlanProductId(it.id)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(id = R.string.logjn_continue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                viewModel.navigateToDedicatedIpLocationSelection()
            }
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = stringResource(id = R.string.cancel),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                viewModel.navigateBack()
            }
            Spacer(modifier = Modifier.weight(1f))
            Footer(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (viewModel.hasAnActivePlaystoreSubscription.value) {
        if (viewModel.showSupportedCountriesDialog.value) {
            DipSignupSupportedCountriesDialog(viewModel = viewModel) {
                viewModel.showSupportedCountriesDialog.value = false
            }
        } else {
            if (viewModel.showFetchingNeededInformationError.value) {
                DipSignupErrorDialog(
                    message = stringResource(id = R.string.dip_signup_required_information_missing_error),
                    confirmButtonMessage = stringResource(id = R.string.take_me_back),
                    onConfirmCallback = {
                        viewModel.navigateBack()
                    },
                )
            }
        }
    } else {
        DipSignupErrorDialog(
            message = stringResource(id = R.string.dip_signup_error),
            confirmButtonMessage = stringResource(id = R.string.take_me_back),
            onConfirmCallback = {
                viewModel.navigateBack()
            },
        )
    }
}

@Composable
private fun DipSignupSupportedCountriesDialog(
    viewModel: DipViewModel,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.dip_signup_supported_countries_title),
                fontSize = 18.sp,
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                viewModel.supportedDipCountriesList.value?.let {
                    LazyColumn {
                        val items = it.dedicatedIpCountriesAvailable
                        items(items.size) { index ->
                            val item: DipCountriesResponse.DedicatedIpCountriesAvailable = items[index]
                            SupportedCountryItem(country = item)
                        }
                    }
                } ?: run {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.i_acknowledge),
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
    )
}

@Composable
fun DipSignupErrorDialog(
    message: String,
    confirmButtonMessage: String,
    onConfirmCallback: () -> Unit,
    onDismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onConfirmCallback,
        title = {
            Text(
                text = stringResource(id = R.string.something_went_wrong),
                fontSize = 18.sp,
                color = LocalColors.current.connectionError(),
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmCallback) {
                Text(
                    text = confirmButtonMessage,
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
        dismissButton = {
            onDismissCallback?.let {
                TextButton(onClick = it) {
                    Text(
                        text = stringResource(id = R.string.take_me_back),
                        fontSize = 14.sp,
                        color = LocalColors.current.primary,
                    )
                }
            }
        },
    )
}

@Composable
fun SupportedCountryItem(
    country: DipCountriesResponse.DedicatedIpCountriesAvailable,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = getFlagResource(LocalContext.current, country.countryCode)),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .size(24.dp)
                .align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(vertical = 8.dp),
        ) {
            SupportedDipRegions(content = country.name)
            Spacer(modifier = Modifier.height(4.dp))
            val regions = country.regions + country.newRegions
            SupportedDipRegionsInCountry(content = regions.joinToString(", "))
        }
    }
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp),
        color = LocalColors.current.outline,
    )
}