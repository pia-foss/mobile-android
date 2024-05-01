package com.kape.dedicatedip.ui.screens.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.dedicatedip.data.models.SupportedCountries
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.SupportedDipRegions
import com.kape.ui.mobile.text.SupportedDipRegionsInCountry
import com.kape.ui.theme.connectionError
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignupDedicatedIpScreen() = Screen {
    val viewModel: DipViewModel = koinViewModel<DipViewModel>().apply {
        getActivePlaystoreSubscription()
        getSupportedDipCountries()
    }
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.dedicated_ip_title))
    }
    val showSupportedCountriesDialog = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateBack() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .semantics {
                    testTagsAsResourceId = true
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        }
    }

    if (viewModel.hasAnActivePlaystoreSubscription.value) {
        if (showSupportedCountriesDialog.value) {
            DipSignupSupportedCountriesDialog(viewModel = viewModel) {
                showSupportedCountriesDialog.value = false
            }
        }
    } else {
        DipSignupErrorDialog() {
            viewModel.navigateBack()
        }
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
                            val item: SupportedCountries.DedicatedIpCountriesAvailable = items[index]
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
private fun DipSignupErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.something_went_wrong),
                fontSize = 18.sp,
                color = LocalColors.current.connectionError(),
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.dip_signup_error),
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.take_me_back),
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
    )
}

@Composable
fun SupportedCountryItem(
    country: SupportedCountries.DedicatedIpCountriesAvailable,
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