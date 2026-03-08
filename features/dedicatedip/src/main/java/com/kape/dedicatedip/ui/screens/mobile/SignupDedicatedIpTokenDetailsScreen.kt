package com.kape.dedicatedip.ui.screens.mobile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.DedicatedIpSignupActivateTokenDescriptionText
import com.kape.ui.mobile.text.DedicatedIpSignupActivateTokenFooter
import com.kape.ui.mobile.text.DedicatedIpSignupActivateTokenTitleText
import com.kape.ui.theme.connectionError
import com.kape.ui.theme.infoBackground
import com.kape.ui.theme.infoBlue
import com.kape.ui.theme.infoOutline
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupDedicatedIpTokenDetailsScreen() = Screen {
    val viewModel: DipViewModel = koinViewModel()
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val toastText = stringResource(id = R.string.dip_signup_token_copied)
    val footerText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = LocalColors.current.onSurface)) {
            append(stringResource(id = R.string.dip_signup_save_your_token_footer_start))
        }
        withStyle(style = SpanStyle(color = LocalColors.current.connectionError())) {
            append(" ${stringResource(id = R.string.dip_signup_save_your_token_footer_end)}")
        }
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color = LocalColors.current.onPrimaryContainer),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = null,
                        tint = LocalColors.current.connectionError(),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DedicatedIpSignupActivateTokenTitleText(
                        content = stringResource(id = R.string.dip_signup_save_your_token_title),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                DedicatedIpSignupActivateTokenDescriptionText(
                    content = stringResource(id = R.string.dip_signup_save_your_token_description),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.height(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LocalColors.current.infoBackground(),
                    ),
                    border = BorderStroke(1.dp, color = LocalColors.current.infoOutline()),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(viewModel.getSignupDipToken()))
                        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                        viewModel.enableActivateTokenButton()
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DedicatedIpSignupActivateTokenDescriptionText(
                            modifier = Modifier.weight(1.0f),
                            content = viewModel.getSignupDipToken(),
                            color = LocalColors.current.infoBlue(),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.content_copy),
                            contentDescription = null,
                            tint = LocalColors.current.infoBlue(),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                DedicatedIpSignupActivateTokenFooter(
                    content = footerText,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.0f))
        PrimaryButton(
            enabled = viewModel.activateTokenButtonState.value,
            text = stringResource(id = R.string.dip_signup_activate_token),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            viewModel.navigateToDedicatedIpTokenActivate()
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}