package com.kape.dedicatedip.ui.screens.mobile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.dedicatedip.domain.DipActivationResult
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.text.DedicatedIpSignupActivateTokenDescriptionText
import com.kape.ui.mobile.text.Input
import com.kape.ui.theme.warningBackground
import com.kape.ui.theme.warningOrange
import com.kape.ui.theme.warningOutline
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignupDedicatedIpTokenActivateScreen() =
    Screen {
        val context = LocalContext.current
        val viewModel: DipViewModel = koinViewModel()
        val showSpinner = rememberSaveable { mutableStateOf(false) }
        val dipToken by viewModel.getSignupDipToken().collectAsStateWithLifecycle()

        Column(
            modifier =
                Modifier
                    .systemBarsPadding()
                    .padding(8.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dip),
                tint = LocalColors.current.primary,
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .height(40.dp)
                        .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                DedicatedIpSignupActivateTokenDescriptionText(
                    content = stringResource(id = R.string.dip_signup_activate_token_description),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Input(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    maskInput = false,
                    keyboard = KeyboardType.Text,
                    content = mutableStateOf(dipToken),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = LocalColors.current.warningBackground(),
                        ),
                    border = BorderStroke(1.dp, color = LocalColors.current.warningOutline()),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { },
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = null,
                            tint = LocalColors.current.warningOrange(),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DedicatedIpSignupActivateTokenDescriptionText(
                            modifier = Modifier.fillMaxWidth(),
                            content = stringResource(id = R.string.dip_signup_activate_token_description_warning),
                            color = LocalColors.current.warningOrange(),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1.0f))
            PrimaryButton(
                enabled = viewModel.activateTokenButtonState.value,
                isLoading = showSpinner.value,
                text = stringResource(id = R.string.activate),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            ) {
                showSpinner.value = true
                viewModel.activateDedicatedIp(
                    mutableStateOf(TextFieldValue(dipToken)),
                )
            }
            Spacer(modifier = Modifier.height(64.dp))
        }

        when (viewModel.activationState.value) {
            DipActivationResult.Active -> {
                showToast(
                    context = context,
                    message = stringResource(id = R.string.dip_success),
                )
                viewModel.resetActivationState()
                showSpinner.value = false
                viewModel.navigateToActivateToken()
            }

            DipActivationResult.Expired -> {
                showToast(
                    context = context,
                    message = stringResource(id = R.string.dip_expired_warning),
                )
                viewModel.resetActivationState()
                showSpinner.value = false
            }

            DipActivationResult.Invalid,
            DipActivationResult.Error,
            -> {
                showToast(
                    context = context,
                    message = stringResource(id = R.string.dip_invalid),
                )
                viewModel.resetActivationState()
                showSpinner.value = false
            }

            null -> {
            }
        }
    }

private fun showToast(
    context: Context,
    message: String,
) {
    Toast
        .makeText(
            context,
            message,
            Toast.LENGTH_LONG,
        ).show()
}