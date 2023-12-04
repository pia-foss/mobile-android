package com.kape.connection.ui.tiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.ui.text.IPText
import com.kape.ui.text.TileTitleText
import com.kape.ui.utils.LocalColors

@Composable
fun Traffic(download: String, upload: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
    ) {
        TileTitleText(content = stringResource(id = R.string.traffic))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(0.4f)) {
                TileTitleText(content = stringResource(id = R.string.download))
                Spacer(modifier = Modifier.height(4.dp))
                IPText(content = download)
            }

            Icon(
                painter = painterResource(id = com.kape.ui.R.drawable.ic_arrow),
                contentDescription = null,
                tint = LocalColors.current.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.2f),
            )

            Column(Modifier.weight(0.4f)) {
                TileTitleText(content = stringResource(id = R.string.upload))
                Spacer(modifier = Modifier.height(4.dp))
                IPText(content = upload)
            }
        }
    }
}