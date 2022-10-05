package com.kape.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.kape.settings.R
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
import com.kape.uicomponents.theme.*

@Composable
fun SettingsScreen() {

    Column {
        AppBar(
            onClick = {

            },
            state = AppBarState(
                stringResource(id = R.string.settings),
                AppBarColors.Default,
                showLogo = false,
                showMenu = false,
                showOverflow = false
            )
        )

        val items = getSettingsList()

        LazyColumn {
            items(items) { item: GeneralSetting ->
                SettingsItem(
                    nameId = item.labelId,
                    iconId = item.iconId,
                    state = if (item.options.isNotEmpty()) item.selectedOption?.let { item.options[it] }
                        ?: item.options[0] else null,
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    nameId: Int,
    iconId: Int,
    state: String? = null,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .background(Color.White)
        .height(Height.SETTINGS_ITEM)
        .clickable {
            onClick.invoke()
        }) {
        Row(
            modifier = modifier
                .padding(Space.SMALL)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = stringResource(
                    id = R.string.icon
                ),
                tint = Color.Unspecified,
                modifier = Modifier.size(Square.ICON)
            )

            Spacer(modifier = Modifier.width(Space.NORMAL))

            if (state == null) {
                Text(text = stringResource(nameId), fontSize = FontSize.Normal, color = Grey20)
            } else {
                Column {
                    Text(text = stringResource(nameId), fontSize = FontSize.Normal, color = Grey20)
                    Text(text = state, fontSize = FontSize.Normal, color = Grey55)
                }
            }
        }
        Icon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(Square.ICON)
                .padding(Space.SMALL),
            painter = painterResource(R.drawable.ic_arrow),
            contentDescription = stringResource(
                id = R.string.icon
            ),
            tint = Color.Unspecified
        )
    }
    Divider(color = Grey85)

}

@Composable
private fun getSettingsList(
    protocolSelection: Int? = null,
    automationSelection: Int? = null
): List<GeneralSetting> {
    val settings = mutableListOf<GeneralSetting>()
    settings.add(GeneralSetting(labelId = R.string.general, R.drawable.ic_setting_general))
    settings.add(
        GeneralSetting(
            labelId = R.string.protocols,
            R.drawable.ic_setting_protocols,
            options = stringArrayResource(
                id = R.array.protocols
            ).toList(), protocolSelection
        )
    )
    settings.add(GeneralSetting(labelId = R.string.networks, R.drawable.ic_setting_network))
    settings.add(GeneralSetting(labelId = R.string.privacy, R.drawable.ic_setting_privacy))
    settings.add(
        GeneralSetting(
            labelId = R.string.automation,
            R.drawable.ic_setting_automation,
            options = stringArrayResource(
                id = R.array.automation
            ).toList(),
            automationSelection
        )
    )
    settings.add(GeneralSetting(labelId = R.string.obfuscation, R.drawable.ic_setting_obfuscation))
    settings.add(GeneralSetting(labelId = R.string.help, R.drawable.ic_setting_help))
    return settings
}

private data class GeneralSetting(
    val labelId: Int,
    val iconId: Int,
    val options: List<String> = emptyList(),
    val selectedOption: Int? = null
)