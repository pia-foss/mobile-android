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

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.username))
        Text(text = "p111111") // TODO get username
        Text(text = stringResource(id = R.string.message_other_devices))
        Row {
            Text(text = stringResource(id = R.string.message_expiration))
            Text(text = "Nov 29, 2024") // TODO get expiration date
        }
    }
}

@Composable
@SuppressLint("ComposableNaming")
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
