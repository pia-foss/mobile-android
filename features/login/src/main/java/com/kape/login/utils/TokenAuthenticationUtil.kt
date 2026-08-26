package com.kape.login.utils

import android.net.Uri
import androidx.core.net.toUri
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.data.auth.ApiResult
import com.kape.permissions.utils.PermissionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class TokenAuthenticationUtil(
    private val dataSource: AuthenticationDataSource,
    private val router: Router,
    private val permissionUtil: PermissionUtil,
) : CoroutineScope {
    private val login = "piavpn:login?"
    private val qrLogin = "piavpn:loginqr?"
    private val loginUrl = "piavpn://login/?"
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun authenticate(uri: Uri) {
        var openUri = uri
        var url = openUri.toString()
        if (url.contains(login)) {
            url = url.replace(login, loginUrl)
        } else if (url.contains(qrLogin)) {
            url = url.replace(qrLogin, loginUrl)
        }
        openUri = url.toUri()
        val token = openUri.getQueryParameter("token")
        token?.let {
            launch {
                val destination =
                    if (dataSource.isUserLoggedIn()) {
                        permissionUtil.getNextDestination()
                    } else {
                        when (dataSource.migrateToken(it)) {
                            is ApiResult.Success -> permissionUtil.getNextDestination()
                            is ApiResult.Error -> LoginWithCredentials
                        }
                    }
                router.updateDestination(destination)
            }
        }
    }
}