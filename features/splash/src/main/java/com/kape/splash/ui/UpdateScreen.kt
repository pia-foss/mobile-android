package com.kape.splash.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.splash.ui.vm.SplashViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdateScreen(viewModel: SplashViewModel = koinViewModel()) = Screen {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 64.dp)
            .background(LocalColors.current.background),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.widthIn(max = 520.dp)) {
            Image(
                painter = painterResource(id = R.drawable.shield),
                contentDescription = null,
                modifier = Modifier
                    .padding(40.dp)
                    .height(140.dp)
                    .fillMaxWidth(),
            )
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                OnboardingTitleText(
                    content = stringResource(id = R.string.update_required_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )

                OnboardingDescriptionText(
                    content = stringResource(id = R.string.update_required_message),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(id = R.string.update_required_action),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 36.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                viewModel.onUpdateClicked(
                    launchUpdate = { url ->
                        val launchIntent = Intent(Intent.ACTION_VIEW)
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        launchIntent.data = Uri.parse(url)
                        // Silently fail if Google Play Store isn't installed.
                        if (launchIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(launchIntent)
                        }
                    },
                )
            }
        }
    }
}