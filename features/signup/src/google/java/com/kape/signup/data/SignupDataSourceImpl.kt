package com.kape.signup.data

import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidVpnSignupInformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

private const val STORE = "google_play"
private const val MAX_RETRY_ATTEMPTS = 3
private const val INITIAL_RETRY_DELAY_MS = 2_000L

// The SDK never surfaces connectivity failures as a thrown exception: it catches them
// internally and reports them through the callback as an AccountRequestError with code >= 600.
// Codes below that are real HTTP responses from the server, i.e. authoritative rejections.
private const val NETWORK_ERROR_CODE = 601

@Singleton([SignupDataSource::class])
class SignupDataSourceImpl(
    private val api: AndroidAccountAPI,
    private val eventGenerator: KpiEventGenerator,
    private val submitKpiEventUseCase: SubmitKpiEventUseCase,
) : SignupDataSource {
    override suspend fun vpnSignup(vararg data: String): Credentials? {
        val receipt =
            AndroidVpnSignupInformation.Receipt(
                data[0],
                data[1],
                data[2],
            )
        val information =
            AndroidVpnSignupInformation(
                store = STORE,
                receipt = receipt,
                obfuscatedDeviceId = data[3],
            )

        for (retryAttempt in 0..MAX_RETRY_ATTEMPTS) {
            val (details, errors) =
                suspendCancellableCoroutine { cont ->
                    api.vpnSignUp(information) { details, error -> cont.resume(details to error) }
                }

            if (details != null) {
                return Credentials(details.status, details.username, details.password)
            }

            if (errors.isNetworkError() && retryAttempt < MAX_RETRY_ATTEMPTS) {
                submitKpiEventUseCase.submitEvent(
                    eventGenerator.getProcessingRetry(
                        retryAttempt + 1,
                        errors.firstOrNull()?.message ?: "Network error",
                    ),
                )
                delay((INITIAL_RETRY_DELAY_MS * 2.0.pow(retryAttempt)).toLong().milliseconds)
                continue
            }
            return null
        }
        return null
    }
}

private fun List<AccountRequestError>.isNetworkError(): Boolean = isNotEmpty() && any { it.code == NETWORK_ERROR_CODE }