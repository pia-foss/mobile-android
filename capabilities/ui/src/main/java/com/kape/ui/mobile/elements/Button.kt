package com.kape.ui.mobile.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.mobile.text.PrimaryButtonText
import com.kape.ui.mobile.text.SecondaryButtonText
import com.kape.ui.utils.LocalColors

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = LocalColors.current.primary,
            contentColor = LocalColors.current.onPrimary,
            disabledContainerColor = LocalColors.current.onSurfaceVariant,
            disabledContentColor = LocalColors.current.outlineVariant,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = LocalColors.current.onPrimary,
            )
        } else {
            PrimaryButtonText(content = text.uppercase())
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LocalColors.current.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LocalColors.current.primary,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = LocalColors.current.onPrimary,
            )
        } else {
            SecondaryButtonText(content = text.uppercase())
        }
    }
}

@Composable
fun OptionButton(selected: Boolean, modifier: Modifier) {
    Icon(
        painter = painterResource(id = if (selected) R.drawable.ic_radio_button_selected else R.drawable.ic_radio_button_default),
        contentDescription = null,
        tint = LocalColors.current.primary,
        modifier = modifier,
    )
}

@Composable
fun Toggle(isOn: Boolean, onCheckedChange: (checked: Boolean) -> Unit) {
    Switch(
        checked = isOn,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            uncheckedBorderColor = LocalColors.current.onSurfaceVariant,
            checkedBorderColor = LocalColors.current.onSurfaceVariant,
            uncheckedTrackColor = LocalColors.current.onPrimary,
            checkedTrackColor = LocalColors.current.primary,
            uncheckedIconColor = LocalColors.current.onSurfaceVariant,
            checkedIconColor = LocalColors.current.onPrimary,
            uncheckedThumbColor = LocalColors.current.onSurfaceVariant,
        ),
    )
}

@Composable
fun FavoriteIcon(isChecked: Boolean, modifier: Modifier) {
    Icon(
        painter = painterResource(id = if (isChecked) R.drawable.ic_heart_selected else R.drawable.ic_heart_default),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier,
    )
}

@Composable
fun Check(isChecked: Boolean, modifier: Modifier) {
    Icon(
        painter = painterResource(id = if (isChecked) R.drawable.ic_check_ticked else R.drawable.ic_check_default),
        contentDescription = null,
        tint = LocalColors.current.primary,
        modifier = modifier,
    )
}

@Composable
fun Visibility(isChecked: Boolean, modifier: Modifier) {
    Icon(
        painter = painterResource(id = if (isChecked) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off),
        contentDescription = null,
        tint = LocalColors.current.onSurface,
        modifier = modifier,
    )
}