package com.kape.login.domain.usecase

import com.kape.account.domain.LoginResult
import com.kape.contracts.AppInfo
import com.kape.contracts.DeviceInfo
import com.kape.featureflags.domain.FeatureFlagRepository
import com.kape.login.domain.SupportTicketInfo
import com.kape.login.domain.buildSupportTicketUrl
import com.kape.login.domain.maskAccountIdentifier
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import org.koin.core.annotation.Factory

private const val SUPPORT_DIALOG_FLAG = "login-support-dialog"

@Factory
class IsLoginSupportEnabledUseCase(
    private val featureFlags: FeatureFlagRepository,
) {
    suspend operator fun invoke(): Boolean = SUPPORT_DIALOG_FLAG in featureFlags.observeFlags().first()
}

@Factory
class BuildLoginSupportTicketUrlUseCase(
    private val appInfo: AppInfo,
    private val deviceInfo: DeviceInfo,
) {
    operator fun invoke(
        failure: LoginResult.ServiceUnavailable,
        username: String,
    ): String =
        buildSupportTicketUrl(
            SupportTicketInfo(
                errorCode = failure.code,
                errorMessage = failure.message,
                timestampUtc = Clock.System.now().toString(),
                appVersionName = appInfo.versionName,
                appVersionCode = appInfo.versionCode,
                osVersion = deviceInfo.osVersion,
                manufacturer = deviceInfo.manufacturer,
                model = deviceInfo.model,
                maskedAccountId = maskAccountIdentifier(username),
            ),
        )
}