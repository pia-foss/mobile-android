package com.kape.login.utils

import android.net.Uri
import com.kape.login.domain.mobile.AuthenticationDataSource
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TokenAuthenticationUtil(
    private val dataSource: AuthenticationDataSource,
    private val router: Router,
) : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun authenticate(uri: Uri) {
        var openUri = uri
        var url = openUri.toString()
        url = url.replace("piavpn:login?", "piavpn://login/?")
        openUri = Uri.parse(url)
        val token = openUri.getQueryParameter("token")
        token?.let {
            launch {
                dataSource.migrateToken(it).collect {
                    router.handleFlow(ExitFlow.Login)
                }
            }
        }
    }
}