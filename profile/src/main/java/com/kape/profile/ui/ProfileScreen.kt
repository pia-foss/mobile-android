package com.kape.profile.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kape.profile.R
import com.kape.profile.di.profileModule
import com.kape.profile.ui.vm.ProfileViewModel
import com.kape.uicomponents.theme.Grey20
import com.kape.uicomponents.theme.Grey55
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ProfileScreen() {
    setupKoinDependencyInjection()

    val viewModel: ProfileViewModel by viewModel()
    val state by remember(viewModel) { viewModel.screenState }.collectAsState()
    viewModel.loadProfile()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(color = Grey55, text = stringResource(id = R.string.username))
        Text(color = Grey20, text = state.username?.asString() ?: "")
        Spacer(modifier = Modifier.height(30.dp))
        Text(color = Grey55, text = stringResource(id = R.string.message_other_devices))
        Spacer(modifier = Modifier.height(15.dp))
        Row {
            Text(color = Grey55, text = state.expirationMessage?.asString() ?: "")
            Spacer(modifier = Modifier.width(3.dp))
            Text(color = Grey20, text = state.expirationDate?.asString() ?: "")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@SuppressLint("ComposableNaming")
@Composable
private fun setupKoinDependencyInjection() {
    if (GlobalContext.getKoinApplicationOrNull() != null) {
        return
    }

    val app: Context = LocalContext.current.applicationContext
    if (app::class != Application::class) {
        return
    }

    startKoin {
        androidContext(androidContext = app)
        modules(mutableListOf<Module>().apply {
            add(profileModule)
        })
    }
}
