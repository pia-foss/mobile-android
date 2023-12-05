package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.kape.signup.R
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.elements.SecondaryButton
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space

@Composable
fun ConsentScreen(viewModel: SignupViewModel) = Screen {
    val showMoreInfo = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_consent),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.MEDIUM),
            )
            Text(
                text = stringResource(id = R.string.consent_title),
                fontSize = FontSize.Big,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            Text(
                text = stringResource(id = R.string.consent_message),
                fontSize = FontSize.Normal,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(Space.MEDIUM),
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            Text(
                text = stringResource(id = R.string.find_out_more).uppercase(),
                fontSize = FontSize.Small,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        showMoreInfo.value = true
                    },
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
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