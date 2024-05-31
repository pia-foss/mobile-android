package com.kape.signup.ui.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.elements.SecondaryButton
import com.kape.ui.tv.text.SignUpTitleText
import com.kape.ui.tv.text.SignupConsentDescriptionText
import com.kape.ui.tv.text.SignupConsentTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvConsentScreen() {
    val viewModel: SignupViewModel = koinViewModel()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_large),
                    contentDescription = null,
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                SignUpTitleText(
                    content = stringResource(id = R.string.signup),
                )
                Spacer(modifier = Modifier.height(64.dp))
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LocalColors.current.onPrimaryContainer,
                    ),
                ) {
                    Image(
                        painter = painterResource(id = com.kape.login.R.drawable.ic_tv_onboarding),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                    )
                }
            }
        }
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 64.dp)
                .width(0.5.dp),
            color = LocalColors.current.primaryContainer,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.ic_consent),
                contentScale = ContentScale.Fit,
                contentDescription = null,
                modifier = Modifier.size(68.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            SignupConsentTitleText(
                content = stringResource(id = R.string.consent_title),
            )
            Spacer(modifier = Modifier.height(32.dp))
            SignupConsentDescriptionText(
                content = stringResource(id = R.string.consent_message),
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(id = R.string.accept),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                viewModel.allowEventSharing(true)
            }
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = stringResource(id = R.string.no_thanks),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                viewModel.allowEventSharing(false)
            }
        }
    }
}