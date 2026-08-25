package com.kape.signup.ui.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kape.signup.utils.Plan
import com.kape.signup.utils.SubscriptionData
import com.kape.ui.R
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.brandGreen
import com.kape.ui.utils.LocalColors

@Composable
internal fun CheckmarkText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            tint = LocalColors.current.brandGreen(),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.widthIn(12.dp))
        Text(
            text = text,
            color = LocalColors.current.onSurface,
            style = PiaTypography.body3,
        )
    }
}

@Composable
internal fun SubscriptionDescriptionText(
    subscriptionData: SubscriptionData,
    selectedPlan: Plan,
) {
    Text(
        text =
            buildAnnotatedString {
                if (selectedPlan == subscriptionData.yearly) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            stringResource(R.string.subscribe_screen_trial_annually_description_headline_to_format)
                                .format(subscriptionData.yearly.mainPrice),
                        )
                    }
                    append(" ${stringResource(id = R.string.subscribe_screen_trial_annually_description)}")
                } else {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(
                            "${subscriptionData.monthly.mainPrice} " +
                                "${stringResource(R.string.subscribe_screen_per_month_ending)}, " +
                                "${
                                    stringResource(R.string.subscribe_screen_billed_monthly)
                                        .toLowerCase(Locale.current)
                                }.",
                        )
                    }
                    append(" ${stringResource(id = R.string.subscribe_screen_trial_monthly_description)}")
                }
            },
        style = PiaTypography.caption1,
        textAlign = TextAlign.Center,
    )
}