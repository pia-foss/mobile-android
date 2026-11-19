package com.kape.connection.ui.mobile

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.view.mobile.AppBarType
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.ui.ConnectButton
import com.kape.connection.ui.tiles.ConnectionTile
import com.kape.connection.ui.vm.ConnectionTilesViewModel
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.customization.data.Element
import com.kape.data.ConnectionStatus
import com.kape.rating.data.RatingDialogType
import com.kape.rating.ui.RatingFeedbackDialog
import com.kape.rating.ui.RatingReviewDialog
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.mobile.elements.RoundIconButton
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.elements.TertiaryButton
import com.kape.ui.mobile.text.DedicatedIpHomeBannerText
import com.kape.ui.theme.PiaTypography.subtitle3
import com.kape.ui.tiles.Dialog
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConnectionScreen(
    drawerContent: @Composable (scope: CoroutineScope, state: DrawerState) -> Unit,
    viewModel: ConnectionViewModel = koinViewModel(),
    tiles: ConnectionTilesViewModel = koinViewModel(),
) = Screen {
    val connectionStatus by viewModel.status.collectAsStateWithLifecycle()
    val elements by viewModel.elements.collectAsStateWithLifecycle()
    val showDedicatedIpBanner by viewModel.showDedicatedIpBanner.collectAsStateWithLifecycle()
    val ratingPrompt by viewModel.ratingPrompt.collectAsStateWithLifecycle()
    val showProtocolNotAvailable by viewModel.showProtocolNotAvailable.collectAsStateWithLifecycle()
    val showAutoProtocolNudge by viewModel.showAutoProtocolNudge.collectAsStateWithLifecycle()
    val hasUpdateAvailable by viewModel.hasUpdateAvailable.collectAsStateWithLifecycle()
    val appBarViewModel: AppBarViewModel = koinViewModel()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalActivity.current
    val context = LocalContext.current

    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            activity?.let(viewModel::checkForPaymentIssues)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerContent(scope, drawerState)
            }
        },
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    viewModel = appBarViewModel,
                    type = AppBarType.Connection,
                    onLeftIconClick = { scope.launch { drawerState.open() } },
                    onRightIconClick = viewModel::navigateToCustomization,
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(snackbarData = data, actionContentColor = LocalColors.current.inversePrimary)
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .semantics { testTagsAsResourceId = true },
                horizontalAlignment = CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    val connectButtonDescription =
                        stringResource(id = R.string.toggle_connection_button) +
                            when (connectionStatus) {
                                ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING ->
                                    stringResource(id = R.string.disconnect_from_vpn)

                                else -> stringResource(id = R.string.connect_to_vpn)
                            }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (showDedicatedIpBanner) {
                        DedicatedIpBanner(
                            onAcceptClick = viewModel::navigateToDedicatedIpPlans,
                            onCancelClick = viewModel::hideDedicatedIpBanner,
                        )
                    }
                    ConnectButton(
                        status = connectionStatus,
                        onTvLayout = false,
                        modifier =
                            Modifier
                                .align(CenterHorizontally)
                                .testTag(":ConnectionScreen:connection_button")
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = connectButtonDescription
                                },
                        onClick = viewModel::onConnectionButtonClicked,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    if (ratingPrompt == RatingDialogType.General) {
                        RatingCard(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            onClose = viewModel::onRatingFinished,
                            onThumbsUp = viewModel::onRatingThumbsUp,
                            onThumbsDown = viewModel::onRatingThumbsDown,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    elements.filter { it.shouldDisplayElement }.forEach { element ->
                        ConnectionTile(element = element.element, tiles = tiles, actions = viewModel)
                        if (element.element.hasSeparator()) Separator()
                    }
                }
            }

            when (ratingPrompt) {
                RatingDialogType.Review -> {
                    val url = stringResource(id = R.string.app_url)
                    RatingReviewDialog(
                        onConfirm = {
                            context.openUrl(url)
                            viewModel.onRatingFinished()
                        },
                        onDismiss = viewModel::onRatingFinished,
                    )
                }

                RatingDialogType.Feedback -> {
                    val url = stringResource(id = R.string.url_support_new_ticket)
                    RatingFeedbackDialog(
                        onConfirm = {
                            context.openUrl(url)
                            viewModel.onFeedbackFinished()
                        },
                        onDismiss = viewModel::onFeedbackFinished,
                    )
                }

                else -> Unit
            }

            if (showProtocolNotAvailable) {
                Dialog(
                    stringResource(R.string.protocol_unavailable_title),
                    stringResource(R.string.protocol_unavailable_message),
                    stringResource(R.string.protocol_unavailable_action),
                    onConfirm = viewModel::dismissProtocolNotAvailable,
                )
            }

            if (showAutoProtocolNudge) {
                Dialog(
                    stringResource(R.string.auto_protocol_nudge_title),
                    stringResource(R.string.auto_protocol_nudge_message),
                    stringResource(R.string.auto_protocol_nudge_confirm),
                    stringResource(R.string.auto_protocol_nudge_dismiss),
                    onConfirm = viewModel::acceptAutomaticProtocol,
                    onDismiss = viewModel::dismissAutoProtocolNudge,
                )
            }

            val updateMessage = stringResource(R.string.app_update_available_title)
            val updateAction = stringResource(R.string.app_update_available_positive_action)
            LaunchedEffect(hasUpdateAvailable) {
                if (!hasUpdateAvailable) return@LaunchedEffect
                val result =
                    snackbarHostState.showSnackbar(
                        message = updateMessage,
                        actionLabel = updateAction,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                    )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.getDownloadUrl()?.let(context::openUrl)
                }
            }
        }
    }
}

private fun Element.hasSeparator() = this != Element.VpnRegionSelection && this != Element.ShadowsocksRegionSelection

private fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
}

@Composable
private fun DedicatedIpBanner(
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
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
                modifier =
                    Modifier
                        .padding(8.dp)
                        .height(90.dp)
                        .constrainAs(image) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
            )
            Column(
                modifier =
                    Modifier.constrainAs(content) {
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
                        modifier =
                            Modifier
                                .background(
                                    LocalColors.current.onPrimary,
                                    shape = CircleShape,
                                ).padding(2.dp),
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
                        modifier =
                            Modifier
                                .background(
                                    LocalColors.current.onPrimary,
                                    shape = CircleShape,
                                ).padding(4.dp),
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
                modifier =
                    Modifier.constrainAs(closeButton) {
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