package com.kape.about.screens.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AboutScreen(licences: List<String>) = Screen {
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.about))
    }

    Scaffold(
        topBar = {
            AppBar(viewModel = appBarViewModel)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 520.dp),
            ) {
                items(licences) {
                    Text(text = it, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}