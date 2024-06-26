package com.kape.ui.mobile.elements

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.mobile.text.BestValueBannerText
import com.kape.ui.mobile.text.ErrorText
import com.kape.ui.mobile.text.InfoText
import com.kape.ui.mobile.text.SettingsL1Text
import com.kape.ui.mobile.text.SettingsL2TextDescription
import com.kape.ui.mobile.text.SignUpDurationText
import com.kape.ui.mobile.text.SignUpPricePerMonthText
import com.kape.ui.mobile.text.SignUpPriceText
import com.kape.ui.theme.errorBackground
import com.kape.ui.theme.errorOutline
import com.kape.ui.theme.infoBackground
import com.kape.ui.theme.infoOutline
import com.kape.ui.theme.successBackground
import com.kape.ui.theme.successOutline
import com.kape.ui.theme.warning30
import com.kape.ui.theme.warningBackground
import com.kape.ui.theme.warningOutline
import com.kape.ui.utils.LocalColors

@Composable
fun YearlySubscriptionCard(
    selected: Boolean,
    price: String,
    perMonthPrice: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .semantics(mergeDescendants = true) { }
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
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

@Composable
fun MonthlySubscriptionCard(
    selected: Boolean,
    price: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .semantics(mergeDescendants = true) { }
            .selectable(selected = selected, enabled = true, role = Role.RadioButton, onClick = onClick),
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ErrorCard(content: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
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

@Composable
fun WarningCard(content: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalColors.current.warningBackground(),
        ),
        border = BorderStroke(1.dp, LocalColors.current.warningOutline()),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error),
                contentDescription = null,
                tint = LocalColors.current.warningOutline(),
            )
            Spacer(modifier = Modifier.width(16.dp))
            ErrorText(content = content, modifier = Modifier.align(CenterVertically))
        }
    }
}

@Composable
fun InfoCard(content: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalColors.current.infoBackground(),
        ),
        border = BorderStroke(1.dp, LocalColors.current.infoOutline()),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info_loading),
                contentDescription = null,
                tint = LocalColors.current.infoOutline(),
            )
            Spacer(modifier = Modifier.width(16.dp))
            InfoText(content = content, modifier = Modifier.align(CenterVertically))
        }
    }
}

@Composable
fun SuccessCard(content: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalColors.current.successBackground(),
        ),
        border = BorderStroke(1.dp, LocalColors.current.successBackground()),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error),
                contentDescription = null,
                tint = LocalColors.current.successOutline(),
            )
            Spacer(modifier = Modifier.width(16.dp))
            ErrorText(content = content, modifier = Modifier.align(CenterVertically))
        }
    }
}

@Composable
fun NetworkCard(
    @DrawableRes icon: Int,
    title: String,
    status: String,
    color: Color,
    isDefault: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalColors.current.surfaceVariant,
        ),
    ) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .fillMaxWidth()
                .background(color),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_horizontal),
                    contentDescription = null,
                    tint = LocalColors.current.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingsL1Text(content = title, modifier = Modifier.wrapContentWidth())
            Spacer(modifier = Modifier.height(16.dp))
            SettingsL2TextDescription(content = status)
        }
    }
}