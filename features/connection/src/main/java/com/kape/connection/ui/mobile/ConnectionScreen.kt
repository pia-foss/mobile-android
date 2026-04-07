package com.kape.connection.ui.mobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.ConnectButton
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.connection.utils.ConnectionScreenState
import com.kape.customization.data.Element
import com.kape.data.ConnectionStatus
import com.kape.data.VpnConnectionInfo
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.rating.data.RatingDialogType
import com.kape.rating.ui.RatingFeedbackDialog
import com.kape.rating.ui.RatingReviewDialog
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.sharedui.tiles.ConnectionInfo
import com.kape.sharedui.tiles.IPTile
import com.kape.sharedui.tiles.QuickConnect
import com.kape.sharedui.tiles.QuickSettings
import com.kape.sharedui.tiles.ShadowsocksLocationPicker
import com.kape.sharedui.tiles.Snooze
import com.kape.sharedui.tiles.Traffic
import com.kape.sharedui.tiles.VpnLocationPicker
import com.kape.sidemenu.ui.screens.mobile.SideMenuContent
import com.kape.ui.R
import com.kape.ui.mobile.elements.InfoCard
import com.kape.ui.mobile.elements.RoundIconButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.elements.TertiaryButton
import com.kape.ui.mobile.text.DedicatedIpHomeBannerText
import com.kape.ui.theme.PiaTypography.subtitle3
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConnectionScreen() = Screen {
    val viewModel: ConnectionViewModel = koinViewModel()
    val vpnState by viewModel.connectionInfoProvider.state.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionInfoProvider.connectionState.collectAsStateWithLifecycle()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val locale = Locale.getDefault().language
    val scope: CoroutineScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val showRatingGeneralDialog = remember { mutableStateOf(false) }
    val showRatingReviewDialog = remember { mutableStateOf(false) }
    val showRatingFeedbackDialog = remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalActivity.current

    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshState()
        }
    }

    LaunchedEffect(connectionState.status) {
        viewModel.refreshState()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.shouldShowDedicatedIpSignupBanner()
        viewModel.autoConnect()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SideMenuContent(scope = scope, state = drawerState)
            }
        },
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    viewModel = appBarViewModel,
                    type = AppBarType.Connection,
                    onLeftIconClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onRightIconClick = { viewModel.navigateToCustomization() },
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .semantics {
                        testTagsAsResourceId = true
                    },
                horizontalAlignment = CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    var connectButtonDescription =
                        stringResource(id = R.string.toggle_connection_button)

                    connectButtonDescription += when (connectionState.status) {
                        ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> stringResource(
                            id = R.string.disconnect_from_vpn,
                        )

                        else -> stringResource(id = R.string.connect_to_vpn)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (viewModel.showDedicatedIpHomeBanner.value) {
                        DedicatedIpBanner(
                            onAcceptClick = {
                                viewModel.navigateToDedicatedIpPlans()
                            },
                            onCancelClick = {
                                viewModel.hideDedicatedIpSignupBanner()
                            },
                        )
                    }
                    ConnectButton(
                        status = connectionState.status,
                        onTvLayout = false,
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .testTag(":ConnectionScreen:connection_button")
                            .semantics(mergeDescendants = true) {
                                role = Role.Button
                                contentDescription = connectButtonDescription
                            },
                    ) {
                        viewModel.onConnectionButtonClicked()
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                    if (showRatingGeneralDialog.value) {
                        RatingCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClose = {
                                viewModel.setRatingStateInactive()
                                showRatingGeneralDialog.value = false
                            },
                            onThumbsUp = viewModel::showReviewPrompt,
                            onThumbsDown = viewModel::showFeedbackPrompt,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    viewModel.getOrderedElements().forEach { screenElement ->
                        DisplayComponent(
                            screenElement = screenElement,
                            isVisible = viewModel.isScreenElementVisible(screenElement),
                            viewModel = viewModel,
                            screenState = state,
                            connectionStatus = connectionState.status,
                            vpnState = vpnState,
                        )
                    }
                    showRatingGeneralDialog.value =
                        state.ratingDialogType is RatingDialogType.General
                    showRatingReviewDialog.value =
                        state.ratingDialogType is RatingDialogType.Review
                    showRatingFeedbackDialog.value =
                        state.ratingDialogType is RatingDialogType.Feedback

                    if (showRatingReviewDialog.value) {
                        val url = stringResource(id = R.string.app_url)
                        val context = LocalContext.current

                        RatingReviewDialog(
                            onConfirm = {
                                val launchIntent = Intent(Intent.ACTION_VIEW)
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                launchIntent.data = Uri.parse(url)

                                // Silently fail if Google Play Store isn't installed.
                                if (launchIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(launchIntent)
                                }
                                viewModel.setRatingStateInactive()
                            },
                            onDismiss = {
                                viewModel.setRatingStateInactive()
                            },
                        )
                    }

                    if (showRatingFeedbackDialog.value) {
                        val url = stringResource(id = R.string.url_support_new_ticket)
                        val context = LocalContext.current

                        RatingFeedbackDialog(
                            onConfirm = {
                                val launchIntent = Intent(Intent.ACTION_VIEW)
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                launchIntent.data = Uri.parse(url)

                                // Silently fail if Google Play Store isn't installed.
                                if (launchIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(launchIntent)
                                }
                            },
                            onDismiss = {
                                viewModel.updateRatingDate()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayComponent(
    screenElement: ScreenElement,
    isVisible: Boolean,
    viewModel: ConnectionViewModel,
    screenState: ConnectionScreenState,
    connectionStatus: ConnectionStatus,
    vpnState: VpnConnectionInfo,
) {
    if (isVisible) {
        val context: Context = LocalContext.current
        when (screenElement.element) {
            Element.ConnectionInfo -> {
                val settings = viewModel.getConnectionSettings()
                ConnectionInfo(
                    connection = settings.name,
                    port = settings.port,
                    auth = settings.auth,
                    transport = settings.transport.value,
                    encryption = settings.dataEncryption.value,
                    handshake = settings.handshake,
                )
            }

            Element.IpInfo -> {

                IPTile(
                    isPortForwardingEnabled = viewModel.isPortForwardingEnabled(),
                    publicIp = vpnState.publicIp,
                    vpnIp = vpnState.vpnIp,
                    portForwardingStatus = when (vpnState.portforwardingStatus) {
                        PortForwardingStatus.Error -> stringResource(id = R.string.pfwd_error)
                        PortForwardingStatus.NoPortForwarding -> stringResource(id = R.string.pfwd_disabled)
                        PortForwardingStatus.Requesting -> stringResource(id = R.string.pfwd_requesting)
                        PortForwardingStatus.Success -> vpnState.port
                    },
                )
                Separator()
            }

            Element.QuickConnect -> {
                val quickConnectMap = mutableMapOf<VpnServer?, Boolean>()
                for (server in screenState.quickConnectServers) {
                    quickConnectMap[server] =
                        viewModel.isVpnServerFavorite(server.name, server.isDedicatedIp)
                }
                QuickConnect(
                    servers = quickConnectMap,
                    onClick = {
                        viewModel.quickConnect(it)
                    },
                )
                Separator()
            }

            Element.QuickSettings -> {
                QuickSettings(
                    onKillSwitchClick = {
                        viewModel.navigateToKillSwitch()
                    },
                    onAutomationClick = {
                        viewModel.navigateToAutomation()
                    },
                    onProtocolsClick = {
                        viewModel.navigateToProtocols()
                    },
                )
                Separator()
            }

            Element.VpnRegionSelection -> {
                VpnLocationPicker(
                    modifier = Modifier.testTag(":ConnectionScreen:VpnLocationPicker"),
                    server = screenState.server,
                    isConnected = connectionStatus == ConnectionStatus.CONNECTED,
                    isOptimal = screenState.isCurrentServerOptimal,
                ) {
                    viewModel.showVpnRegionSelection()
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (screenState.showOptimalLocationInfo) {
                    InfoCard(
                        content = stringResource(id = R.string.optimal_location_hint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
            }

            Element.ShadowsocksRegionSelection -> {
                ShadowsocksLocationPicker(
                    server = viewModel.getSelectedShadowsocksServer(),
                    isConnected = connectionStatus == ConnectionStatus.CONNECTED,
                ) {
                    viewModel.showShadowsocksRegionSelection()
                }
            }

            Element.Snooze -> {
                Snooze(
                    active = viewModel.isSnoozeActive,
                    timeUntilResume = when (viewModel.timeUntilResume.intValue) {
                        1 -> String.format(
                            stringResource(id = R.string.minute_to_format),
                            viewModel.timeUntilResume.intValue,
                        )

                        else -> String.format(
                            stringResource(id = R.string.minutes_to_format),
                            viewModel.timeUntilResume.intValue,
                        )
                    },
                    onClick = {
                        if (connectionStatus == ConnectionStatus.CONNECTED) {
                            viewModel.snooze(it)
                        }
                    },
                    onResumeClick = {
                        viewModel.onSnoozeResumed()
                    },
                )
                Separator()
            }

            Element.Traffic -> {
                Traffic(
                    download = viewModel.download.value,
                    upload = viewModel.upload.value,
                )
                Separator()
            }
        }
    }
}

@Composable
private fun DedicatedIpBanner(
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(LocalColors.current.primary)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DedicatedIpHomeBannerText(
            modifier = Modifier.weight(1.0f),
            content = stringResource(id = R.string.dip_signup_banner_home_description),
        )
        Spacer(modifier = Modifier.width(8.dp))
        TertiaryButton(
            text = stringResource(id = R.string.yes_i_want),
            onClick = onAcceptClick,
        )
        RoundIconButton(
            painterId = R.drawable.ic_close,
            onClick = onCancelClick,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun RatingCard(
    modifier: Modifier,
    onClose: () -> Unit,
    onThumbsUp: () -> Unit,
    onThumbsDown: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalColors.current.outline),
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val (image, content, closeButton) = createRefs()
            Image(
                painterResource(R.drawable.img_rating),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .height(90.dp)
                    .constrainAs(image) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            )
            Column(
                modifier = Modifier.constrainAs(content) {
                    start.linkTo(image.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(closeButton.start)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.rating_prompt),
                    style = subtitle3,
                    color = LocalColors.current.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    IconButton(
                        onClick = onThumbsDown,
                        modifier = Modifier
                            .background(
                                LocalColors.current.onPrimary,
                                shape = CircleShape,
                            )
                            .padding(2.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_thumbs_down),
                            contentDescription = null,
                            tint = LocalColors.current.onBackground,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onThumbsUp,
                        modifier = Modifier
                            .background(
                                LocalColors.current.onPrimary,
                                shape = CircleShape,
                            )
                            .padding(4.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_thumbs_up),
                            contentDescription = null,
                            tint = LocalColors.current.onBackground,
                        )
                    }
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.constrainAs(closeButton) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                },
            ) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = LocalColors.current.onBackground,
                )
            }
        }
    }
}