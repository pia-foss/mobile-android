package com.kape.automation.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.ui.elements.BehaviorDialog
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.NetworkCard
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.Hyperlink
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingTitleText
import com.kape.ui.theme.infoBlue
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationScreen() =
    Screen {
        val viewModel: AutomationViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.trusted_network_plural))
            }
        val showDialog = remember { mutableStateOf(false) }
        val currentItem = remember { mutableStateOf<NetworkItem?>(null) }
        val context: Context = LocalContext.current
        val rules by viewModel.rules.collectAsStateWithLifecycle(emptyList())

        AutomationScreenContent(
            appBarViewModel,
            onNetworkCardClick = { item ->
                currentItem.value = item
                showDialog.value = true
            },
            viewModel::navigateToAutomationAddNewRule,
            rules,
        )

        if (showDialog.value) {
            val removeRule = stringResource(id = R.string.nmt_remove_rule)
            currentItem.value?.let { rule ->
                BehaviorDialog(
                    status = getStatus(behavior = rule.networkBehavior),
                    showRemoveOption = !rule.isDefault,
                    showDialog,
                ) {
                    if (it == removeRule) {
                        viewModel.removeRule(context, rule)
                    } else {
                        rule.let { item ->
                            viewModel.updateRule(
                                context,
                                item,
                                getRuleForStatus(context, status = it),
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun AutomationScreenContent(
    appBarViewModel: AppBarViewModel,
    onNetworkCardClick: (NetworkItem) -> Unit,
    navigateToAddRule: () -> Unit,
    rules: List<NetworkItem>,
) {
    Scaffold(
        topBar = {
            AppBar(viewModel = appBarViewModel)
        },
    ) {
        AdaptiveSplitContent(
            modifier =
                Modifier
                    .padding(it)
                    .semantics { testTagsAsResourceId = true },
            header = { AutomationInformation() },
            body = { isCompact ->
                if (!isCompact) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                AutomationRules(
                    rules = rules,
                    onNetworkCardClick = onNetworkCardClick,
                    navigateToAddRule = navigateToAddRule,
                )
            },
        )
    }
}

@Composable
private fun AutomationInformation() {
    OnboardingTitleText(
        content = stringResource(id = R.string.manage_automation_title),
        modifier = Modifier.padding(16.dp),
    )

    OnboardingDescriptionText(
        content = stringResource(id = R.string.manage_automation_description),
        modifier = Modifier.padding(16.dp),
    )

    OnboardingDescriptionText(
        content = stringResource(id = R.string.manage_automation_snooze_notice),
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
    )
}

/**
 * The rule cards, two per row, with "Add rule" after them. Laid out eagerly: there are only a
 * few rules, and the screen scrolls as a whole.
 */
@Composable
private fun AutomationRules(
    rules: List<NetworkItem>,
    onNetworkCardClick: (NetworkItem) -> Unit,
    navigateToAddRule: () -> Unit,
) {
    rules.chunked(RULES_PER_ROW).forEach { row ->
        // Sized to the tallest card, which the others then fill. With the cards' own 8dp all round,
        // 16dp at the edges and between cards and rows.
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(IntrinsicSize.Min),
        ) {
            row.forEach { networkItem ->
                RuleCard(
                    networkItem = networkItem,
                    onClick = { onNetworkCardClick(networkItem) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                )
            }
            repeat(RULES_PER_ROW - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        modifier =
            Modifier.clickable {
                navigateToAddRule()
            },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = null,
            tint = LocalColors.current.primary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Hyperlink(
            content = stringResource(id = R.string.manage_automation_add),
            modifier = Modifier.align(CenterVertically),
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun RuleCard(
    networkItem: NetworkItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon: Int
    val title: String
    val status = getStatus(behavior = networkItem.networkBehavior)

    when (networkItem.networkType) {
        NetworkType.MobileData -> {
            icon = R.drawable.ic_mobile_data
            title =
                stringResource(id = R.string.nmt_mobile_data)
        }

        NetworkType.WifiCustom -> {
            icon = R.drawable.ic_wifi_custom
            title = networkItem.networkName
        }

        NetworkType.WifiOpen -> {
            icon = R.drawable.ic_open_wifi
            title = networkItem.networkName
        }

        NetworkType.WifiSecure -> {
            icon = R.drawable.ic_secure_wifi
            title =
                stringResource(id = R.string.nmt_secure_wifi)
        }
    }
    NetworkCard(
        icon = icon,
        title = title,
        status = status,
        color =
            when (networkItem.networkBehavior) {
                NetworkBehavior.AlwaysConnect -> LocalColors.current.primary
                NetworkBehavior.AlwaysDisconnect -> LocalColors.current.error
                NetworkBehavior.RetainState -> LocalColors.current.infoBlue()
            },
        isDefault = networkItem.isDefault,
        onClick = onClick,
        modifier = modifier,
        testTag = ":AutomationScreen:networkCard_${networkItem.networkType}",
    )
}

private const val RULES_PER_ROW = 2

@Composable
private fun getStatus(behavior: NetworkBehavior): String =
    when (behavior) {
        NetworkBehavior.AlwaysConnect -> stringResource(id = R.string.nmt_connect)
        NetworkBehavior.AlwaysDisconnect -> stringResource(id = R.string.nmt_disconnect)
        NetworkBehavior.RetainState -> stringResource(id = R.string.nmt_retain)
    }

fun getRuleForStatus(
    context: Context,
    status: String,
): NetworkBehavior =
    when (status) {
        context.getString(R.string.nmt_retain) -> NetworkBehavior.RetainState
        context.getString(R.string.nmt_disconnect) -> NetworkBehavior.AlwaysDisconnect
        else -> NetworkBehavior.AlwaysConnect
    }