@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kape.ui.tv.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.kape.ui.R
import com.kape.ui.mobile.elements.OptionButton
import com.kape.ui.mobile.text.BestValueBannerText
import com.kape.ui.mobile.text.SignUpDurationText
import com.kape.ui.mobile.text.SignUpPricePerMonthText
import com.kape.ui.mobile.text.SignUpPriceText
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.onSuccessContainer
import com.kape.ui.theme.successBackground
import com.kape.ui.theme.warning30
import com.kape.ui.utils.LocalColors

@Composable
fun YearlySubscriptionCard(
    selected: Boolean,
    price: String,
    perMonthPrice: String,
    modifier: Modifier,
    freeTrialDays: Int?,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.semantics(mergeDescendants = true) { },
        onClick = onClick,
        shape =
            CardDefaults.shape(
                shape = RoundedCornerShape(12.dp),
            ),
        colors =
            CardDefaults.colors(
                containerColor = LocalColors.current.onPrimaryContainer,
                contentColor = LocalColors.current.onSurfaceVariant,
                focusedContentColor = LocalColors.current.onSurfaceVariant,
            ),
        border =
            if (selected) {
                CardDefaults.border(
                    border = Border(BorderStroke(2.dp, LocalColors.current.primary)),
                    focusedBorder = Border(BorderStroke(2.dp, LocalColors.current.primary)),
                )
            } else {
                CardDefaults.border(
                    border = Border(BorderStroke(1.dp, LocalColors.current.onSurface)),
                    focusedBorder = Border(BorderStroke(2.dp, LocalColors.current.primary)),
                )
            },
    ) {
        Row {
            OptionButton(
                selected = selected,
                modifier =
                    Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp),
            )
            Column {
                SignUpDurationText(
                    content = stringResource(id = R.string.yearly),
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = PiaTypography.subtitle3,
                )
                Row {
                    SignUpPriceText(
                        content = price,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    SignUpPricePerMonthText(
                        content = perMonthPrice,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
                Row(modifier = Modifier.padding(vertical = 16.dp)) {
                    BestValueBannerText(
                        content = stringResource(id = R.string.best_value),
                        modifier =
                            Modifier
                                .background(
                                    LocalColors.current.warning30(),
                                    shape = RoundedCornerShape(4.dp),
                                ).padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    freeTrialDays?.let {
                        Text(
                            text = stringResource(R.string.free_trial_to_format, it),
                            style = PiaTypography.caption1,
                            color = LocalColors.current.onSuccessContainer(),
                            modifier =
                                Modifier
                                    .background(
                                        LocalColors.current.successBackground(),
                                        RoundedCornerShape(4.dp),
                                    ).padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySubscriptionCard(
    selected: Boolean,
    price: String,
    modifier: Modifier,
    freeTrialDays: Int?,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.semantics(mergeDescendants = true) { },
        onClick = onClick,
        shape =
            CardDefaults.shape(
                shape = RoundedCornerShape(12.dp),
            ),
        colors =
            CardDefaults.colors(
                containerColor = LocalColors.current.onPrimaryContainer,
                contentColor = LocalColors.current.onSurfaceVariant,
                focusedContentColor = LocalColors.current.onSurfaceVariant,
            ),
        border =
            if (selected) {
                CardDefaults.border(
                    border = Border(BorderStroke(2.dp, LocalColors.current.primary)),
                    focusedBorder = Border(BorderStroke(2.dp, LocalColors.current.primary)),
                )
            } else {
                CardDefaults.border(
                    border = Border(BorderStroke(1.dp, LocalColors.current.onSurface)),
                    focusedBorder = Border(BorderStroke(2.dp, LocalColors.current.primary)),
                )
            },
    ) {
        Row {
            OptionButton(
                selected = selected,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterVertically),
            )
            Column {
                SignUpDurationText(
                    content = stringResource(id = R.string.monthly),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
                SignUpPriceText(
                    content = price,
                    modifier = Modifier.wrapContentWidth(),
                )
                Row(modifier = Modifier.padding(vertical = 16.dp)) {
                    freeTrialDays?.let {
                        Text(
                            text = stringResource(R.string.free_trial_to_format, it),
                            style = PiaTypography.caption1,
                            color = LocalColors.current.onSuccessContainer(),
                            modifier =
                                Modifier
                                    .background(
                                        LocalColors.current.successBackground(),
                                        RoundedCornerShape(4.dp),
                                    ).padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}