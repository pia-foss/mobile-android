package com.kape.signup.ui.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.InsetsColumn
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.SecondaryButton

@Composable
fun ConsentScreen(viewModel: SignupViewModel) = Screen {
    val showMoreInfo = remember { mutableStateOf(false) }

    InsetsColumn {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Image(
                    painter = painterResource(id = com.kape.signup.R.drawable.ic_consent),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                )
                Text(
                    text = stringResource(id = R.string.consent_title),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.consent_message),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.find_out_more).uppercase(),
                    fontSize = 12.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            showMoreInfo.value = true
                        },
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(id = R.string.accept),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    viewModel.allowEventSharing(true)
                }
                SecondaryButton(
                    text = stringResource(id = R.string.no_thanks),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag(":SignUpScreen:Login"),
                ) {
                    viewModel.allowEventSharing(false)
                }
            }
        }
    }
}