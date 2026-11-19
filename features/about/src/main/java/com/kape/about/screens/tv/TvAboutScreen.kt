package com.kape.about.screens.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.kape.about.ui.vm.AboutViewModel
import com.kape.appbar.view.topBarColor
import com.kape.contracts.ConnectionStatusSource
import com.kape.ui.R
import com.kape.ui.Screen
import com.kape.ui.tv.elements.AboutButton
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.utils.LocalColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TvAboutScreen() =
    Screen {
        val connectionStatus by koinInject<ConnectionStatusSource>().status.collectAsStateWithLifecycle()
        val licences by koinViewModel<AboutViewModel>().licences.collectAsStateWithLifecycle()

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 4.dp,
                color = connectionStatus.topBarColor(LocalColors.current),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                        .background(LocalColors.current.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppBarTitleText(
                        content = stringResource(id = R.string.about),
                        textColor = LocalColors.current.onSurface,
                        isError = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                LazyVerticalGrid(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                    columns = GridCells.Fixed(1),
                ) {
                    items(licences.size) { index ->
                        val license = licences[index]
                        AboutButton {
                            Text(text = license)
                        }
                    }
                }
            }
        }
    }