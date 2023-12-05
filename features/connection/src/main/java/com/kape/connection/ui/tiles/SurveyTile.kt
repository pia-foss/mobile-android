package com.kape.connection.ui.tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.connection.R
import com.kape.ui.utils.LocalColors

@Composable
fun SurveyTile(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_alert_green),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.weight(0.1f),
            )
            Text(
                text = stringResource(id = R.string.survey_message),
                fontSize = 14.sp,
                color = LocalColors.current.onSurface,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(0.8f),
            )
            Icon(
                painter = painterResource(id = com.kape.ui.R.drawable.ic_close),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(0.1f)
                    .clickable {
                    },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.survey_action),
            fontSize = 14.sp,
            color = LocalColors.current.primary,
            modifier = Modifier
                .padding(horizontal = 44.dp)
                .clickable {
                    onClick.invoke()
                },
        )
    }
}