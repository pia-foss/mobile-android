package com.kape.ui.mobile.elements

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.contracts.Router
import com.kape.ui.utils.DeviceConfiguration
import org.koin.compose.koinInject

@Composable
fun Screen(content: @Composable () -> Unit) {
    val router: Router = koinInject()

    BackHandler {
        router.navigateBack()
    }
    content()
}

@Composable
fun AdaptiveScreen(
    mobilePortrait: @Composable (modifier: Modifier) -> Unit,
    mobileLandscape: @Composable (modifier: Modifier) -> Unit = mobilePortrait,
    tabletAndDesktop: @Composable (modifier: Modifier) -> Unit = mobilePortrait,
) = Screen {
    val deviceConfiguration =
        DeviceConfiguration.fromWindowSizeClass(currentWindowAdaptiveInfoV2().windowSizeClass)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    ) { paddingValues ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            val rootModifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .semantics { testTagsAsResourceId = true }

            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT -> mobilePortrait(rootModifier)
                DeviceConfiguration.MOBILE_LANDSCAPE -> mobileLandscape(rootModifier)
                DeviceConfiguration.TABLET_PORTRAIT,
                DeviceConfiguration.TABLET_LANDSCAPE,
                DeviceConfiguration.DESKTOP,
                -> tabletAndDesktop(rootModifier)
            }
        }
    }
}