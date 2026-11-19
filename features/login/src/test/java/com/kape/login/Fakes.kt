package com.kape.login

import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import com.kape.contracts.DebugLogCleaner
import com.kape.contracts.PermissionStatus
import com.kape.contracts.VpnConnectionControl
import com.kape.login.domain.LocalSessionRepository

internal class Recorder {
    val events = mutableListOf<String>()
}

internal class FakeAuthentication(
    private val recorder: Recorder = Recorder(),
) : AuthenticationRepository {
    var loggedIn = false
    var result: LoginResult = LoginResult.Success
    var logoutConfirmed = true

    override suspend fun isLoggedIn() = loggedIn

    override suspend fun loginWithCredentials(
        username: String,
        password: String,
    ) = result.also { recorder.events += "login($username)" }

    override suspend fun requestLoginLink(email: String) = result.also { recorder.events += "link($email)" }

    override suspend fun loginWithReceipt(
        purchaseToken: String,
        productId: String,
        packageName: String,
    ) = result.also { recorder.events += "receipt($purchaseToken, $productId, $packageName)" }

    override suspend fun loginWithToken(apiToken: String) = result.also { recorder.events += "token($apiToken)" }

    override suspend fun logout() = logoutConfirmed.also { recorder.events += "server logout" }
}

internal class FakePermissions(
    var vpnAllowed: Boolean = true,
    var notificationsAllowed: Boolean = true,
) : PermissionStatus {
    override fun isVpnProfileInstalled() = vpnAllowed

    override fun areNotificationsAllowed() = notificationsAllowed
}

internal class FakeLocalSession(
    private val recorder: Recorder,
) : LocalSessionRepository {
    var automationEnabled = false

    override suspend fun isAutomationEnabled() = automationEnabled

    override suspend fun markDisconnectedByUser() {
        recorder.events += "mark disconnected by user"
    }

    override suspend fun clear() {
        recorder.events += "clear local data"
    }
}

internal class FakeVpn(
    private val recorder: Recorder,
) : VpnConnectionControl {
    var connected = false
    var connecting = false

    override fun isConnected() = connected

    override fun isConnectionInProgress() = connecting

    override suspend fun disconnect() {
        recorder.events += "disconnect"
    }
}

internal class FakeDebugLogs(
    private val recorder: Recorder,
) : DebugLogCleaner {
    override fun scheduleClearOnSessionEnd() {
        recorder.events += "schedule log clear"
    }

    override suspend fun clearDebugLogs() {
        recorder.events += "clear logs"
    }
}