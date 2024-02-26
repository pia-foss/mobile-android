package com.kape.login.utils

import android.net.Uri
import com.kape.login.domain.AuthenticationDataSource
import com.kape.router.ExitFlow
import com.kape.router.Router
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TokenAuthenticationUtil(
    private val dataSource: AuthenticationDataSource,
    private val router: Router,
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun authenticate(uri: Uri) {
        var openUri = uri
        var url = openUri.toString()
        url = url.replace("piavpn:login?", "piavpn://login/?")
        openUri = Uri.parse(url)
        val token = openUri.getQueryParameter("token")
        token?.let {
            GlobalScope.launch {
                dataSource.migrateToken(it).collect {
                    router.handleFlow(ExitFlow.Login)
                }
            }
        }
    }
}