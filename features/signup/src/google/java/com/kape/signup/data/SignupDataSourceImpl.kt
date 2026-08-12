package com.kape.signup.data

import com.kape.shareevents.data.KpiEventGenerator
import com.kape.shareevents.domain.SubmitKpiEventUseCase
import com.kape.signup.data.models.Credentials
import com.kape.signup.domain.SignupDataSource
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidVpnSignupInformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

private const val STORE = "google_play"
private const val MAX_RETRY_ATTEMPTS = 3
private const val INITIAL_RETRY_DELAY_MS = 2_000L

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

        for (retryAttempt in 0..MAX_RETRY_ATTEMPTS) {
            try {
                return suspendCancellableCoroutine { cont ->
                    api.vpnSignUp(
                        AndroidVpnSignupInformation(
                            store = STORE,
                            receipt = receipt,
                            obfuscatedDeviceId = data[3],
                        ),
                    ) { details, error ->
                        // Server replied: never retry.
                        if (error.isNotEmpty() || details == null) {
                            cont.resume(null)
                            return@vpnSignUp
                        }

                        cont.resume(
                            Credentials(
                                details.status,
                                details.username,
                                details.password,
                            ),
                        )
                    }
                }
            } catch (e: IOException) {
                // No retries left.
                if (retryAttempt == MAX_RETRY_ATTEMPTS) {
                    return null
                }

                submitKpiEventUseCase.submitEvent(
                    eventGenerator.getProcessingRetry(
                        retryAttempt + 1,
                        e.message ?: "IOException",
                    ),
                )
                val delayMs =
                    (INITIAL_RETRY_DELAY_MS * 2.0.pow(retryAttempt)).toLong()
                delay(delayMs.milliseconds)
            }
        }

        return null
    }
}