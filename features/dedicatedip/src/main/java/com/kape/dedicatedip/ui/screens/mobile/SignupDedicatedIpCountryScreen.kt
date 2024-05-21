package com.kape.dedicatedip.ui.screens.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.dedicatedip.data.models.SupportedCountries
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.HtmlText
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.text.DedicatedIpSignupCountryDisclaimerText
import com.kape.ui.mobile.text.DedicatedIpSignupCountryTitleText
import com.kape.ui.mobile.text.SupportedDipCountryOnly
import com.kape.ui.mobile.text.SupportedDipRegions
import com.kape.ui.mobile.text.SupportedDipRegionsInCountry
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignupDedicatedIpCountryScreen() = Screen {
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.dedicated_ip_title))
    }
    val viewModel: DipViewModel = koinViewModel<DipViewModel>().apply {
        getActivePlaystoreSubscription()
        getSupportedDipCountries()
        getDipMonthlyPlan()
        getDipYearlyPlan()
    }

    val showAllLocations = remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            DedicatedIpSignupCountryTitleText(
                stringResource(id = R.string.dip_signup_country_title),
                Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .border(
                        0.5.dp,
                        LocalColors.current.outlineVariant,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .background(
                        LocalColors.current.onPrimary,
                        shape = RoundedCornerShape(12.dp),
                    ),
            ) {
                viewModel.dipSelectedCountry.value?.let {
                    DipCountryItem(it, false) {
                        showAllLocations.value = !showAllLocations.value
                    }
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                )
            }

            if (showAllLocations.value) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LocalColors.current.onPrimary),
                ) {
                    viewModel.supportedDipCountriesList.value?.let {
                        FlowColumn {
                            it.dedicatedIpCountriesAvailable.forEach {
                                DipCountryItem(country = it, showDetails = true) {
                                    viewModel.selectDipCountry(it)
                                    showAllLocations.value = !showAllLocations.value
                                }
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp),
                                    color = LocalColors.current.outline,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            DedicatedIpSignupCountryDisclaimerText(
                stringResource(id = R.string.dip_signup_country_disclaimer),
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            if (showAllLocations.value) {
                BottomScreen(showAllLocations = showAllLocations.value, viewModel = viewModel)
            }
        }
        Column {
            if (!showAllLocations.value) {
                BottomScreen(showAllLocations = showAllLocations.value, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BottomScreen(showAllLocations: Boolean, viewModel: DipViewModel) {
    Column(
        modifier = Modifier
            .padding(8.dp),
    ) {
        if (!showAllLocations) {
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(id = R.string.logjn_continue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            viewModel.signup()
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
        Spacer(modifier = Modifier.height(16.dp))
        HtmlText(
            textId = R.string.footer,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DipCountryItem(
    country: SupportedCountries.DedicatedIpCountriesAvailable,
    showDetails: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .clickable {
                onClick()
            },
    ) {
        Image(
            painter = painterResource(
                id = getFlagResource(
                    LocalContext.current,
                    country.countryCode,
                ),
            ),
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
            if (showDetails) {
                SupportedDipRegions(content = country.name)
                Spacer(modifier = Modifier.height(4.dp))
                val regions = country.regions + country.newRegions
                SupportedDipRegionsInCountry(content = regions.joinToString(", "))
            } else {
                SupportedDipCountryOnly(content = country.name)
            }
        }
    }
}