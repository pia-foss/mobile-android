package com.kape.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kape.ui.text.PrimaryButtonText
import com.kape.ui.text.SecondaryButtonText
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
        modifier = modifier,
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
        modifier = modifier,
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
    RadioButton(selected = selected, onClick = { }, modifier = modifier)
}

@Composable
fun Toggle(isOn: Boolean, onCheckedChange: (checked: Boolean) -> Unit, modifier: Modifier) {
    Switch(
        checked = isOn, onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            uncheckedBorderColor = LocalColors.current.onSurfaceVariant,
            checkedBorderColor = LocalColors.current.onSurfaceVariant,
            uncheckedTrackColor = LocalColors.current.onPrimary,
            checkedTrackColor = LocalColors.current.primary,
            uncheckedIconColor = LocalColors.current.onSurfaceVariant,
            checkedIconColor = LocalColors.current.onPrimary,
        ),
        modifier = modifier,
    )
}