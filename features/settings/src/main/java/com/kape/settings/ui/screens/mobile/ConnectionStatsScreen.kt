package com.kape.settings.ui.screens.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.ui.vm.ConnectionStatsViewModel
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConnectionStatsScreen() =
    Screen {
        val viewModel: ConnectionStatsViewModel = koinViewModel()
        val eventListState by viewModel.eventList.collectAsStateWithLifecycle()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.connection_stats_title))
            }

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel)
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(it)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = Modifier.widthIn(max = 520.dp)) {
                    if (eventListState.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.connection_stats_no_events),
                            color = LocalColors.current.outlineVariant,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        LazyColumn {
                            items(eventListState) {
                                Text(
                                    text = it,
                                    color = LocalColors.current.outlineVariant,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(4.dp),
                                    textAlign = TextAlign.Start,
                                )
                            }
                        }
                    }
                }
            }
        }
    }