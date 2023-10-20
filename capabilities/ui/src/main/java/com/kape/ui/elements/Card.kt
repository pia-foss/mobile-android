package com.kape.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.text.BestValueBannerText
import com.kape.ui.text.ErrorText
import com.kape.ui.text.SignUpDurationText
import com.kape.ui.text.SignUpPricePerMonthText
import com.kape.ui.text.SignUpPriceText
import com.kape.ui.theme.errorBackground
import com.kape.ui.theme.errorOutline
import com.kape.ui.theme.warning30
import com.kape.ui.utils.LocalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlySubscriptionCard(
    selected: Boolean,
    price: String,
    perMonthPrice: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LocalColors.current.surface else Color.Transparent,
        ),
        border = if (selected) {
            BorderStroke(2.dp, LocalColors.current.primary)
        } else {
            BorderStroke(1.dp, LocalColors.current.onSurface)
        },
    ) {
        Row {
            OptionButton(
                selected = selected,
                modifier = Modifier
                    .align(CenterVertically)
                    .padding(16.dp),
            )
            Column {
                SignUpDurationText(
                    content = stringResource(id = R.string.yearly),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                Row {
                    SignUpPriceText(
                        content = price,
                        modifier = Modifier.align(CenterVertically),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    SignUpPricePerMonthText(
                        content = perMonthPrice,
                        modifier = Modifier.align(CenterVertically),
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .wrapContentWidth()
                        .background(
                            LocalColors.current.warning30(),
                            shape = RoundedCornerShape(4.dp),
                        ),
                ) {
                    BestValueBannerText(
                        content = stringResource(id = R.string.best_value),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySubscriptionCard(
    selected: Boolean,
    price: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) LocalColors.current.surface else Color.Transparent,
        ),
        border = if (selected) {
            BorderStroke(2.dp, LocalColors.current.primary)
        } else {
            BorderStroke(1.dp, LocalColors.current.onSurface)
        },
    ) {
        Row {
            OptionButton(
                selected = selected,
                modifier = Modifier
                    .padding(16.dp)
                    .align(CenterVertically),

                )
            Column {
                SignUpDurationText(
                    content = stringResource(id = R.string.monthly),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                SignUpPriceText(
                    content = price,
                    modifier = Modifier.wrapContentWidth(),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ErrorCard(content: String, modifier: Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalColors.current.errorBackground(),
        ),
        border = BorderStroke(1.dp, LocalColors.current.errorOutline()),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(16.dp))
            ErrorText(content = content, modifier = Modifier.align(CenterVertically))
        }
    }
}