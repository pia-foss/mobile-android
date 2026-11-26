package com.kape.signup.data

import android.content.Context
import android.provider.Settings
import com.kape.account.domain.AuthenticationRepository
import com.kape.account.domain.LoginResult
import com.kape.contracts.PurchaseEvents
import com.kape.data.model.PurchaseData
import com.kape.payments.domain.VpnSubscriptionBilling
import com.kape.signup.domain.Credentials
import com.kape.signup.domain.PurchaseSignupRepository
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.request.AndroidVpnSignupInformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import java.security.MessageDigest
import java.util.Base64
import kotlin.coroutines.resume
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

private const val RECEIPT_STORE = "google_play"
private const val MAX_RETRIES = 3
private const val INITIAL_RETRY_DELAY_MS = 2_000L

// The account library reports connectivity failures through the callback with this code;
// codes below 600 are real server responses, which retrying won't change.
private const val NETWORK_ERROR_CODE = 601

sealed interface AccountCreation {
    data class Created(
        val credentials: Credentials,
    ) : AccountCreation

    data class NetworkError(
        val message: String,
    ) : AccountCreation

    data object Rejected : AccountCreation
}

interface AccountSignupDataSource {
    suspend fun createAccount(
        purchase: PurchaseData,
        obfuscatedDeviceId: String,
    ): AccountCreation

    suspend fun setEmail(email: String): Boolean
}

fun interface ObfuscatedDeviceIdentifier {
    operator fun invoke(): String?
}

@Singleton(binds = [PurchaseSignupRepository::class])
class PlaySignupRepository(
    private val billing: VpnSubscriptionBilling,
    private val accounts: AccountSignupDataSource,
    private val authentication: AuthenticationRepository,
    private val deviceIdentifier: ObfuscatedDeviceIdentifier,
    private val events: PurchaseEvents,
) : PurchaseSignupRepository {
    override suspend fun signUp(email: String): Credentials? {
        val purchase = billing.pendingPurchase() ?: return null
        val deviceId = deviceIdentifier() ?: return null
        val credentials = createAccount(purchase, deviceId) ?: return null
        if (authentication.loginWithCredentials(credentials.username, credentials.password) != LoginResult.Success) return null
        if (!accounts.setEmail(email)) return null
        billing.clearPendingPurchase()
        return credentials
    }

    private suspend fun createAccount(
        purchase: PurchaseData,
        deviceId: String,
    ): Credentials? {
        repeat(MAX_RETRIES + 1) { attempt ->
            when (val result = accounts.createAccount(purchase, deviceId)) {
                is AccountCreation.Created -> return result.credentials
                AccountCreation.Rejected -> return null
                is AccountCreation.NetworkError -> {
                    if (attempt == MAX_RETRIES) return null
                    events.signupRetried(attempt + 1, result.message)
                    delay((INITIAL_RETRY_DELAY_MS * 2.0.pow(attempt)).toLong().milliseconds)
                }
            }
        }
        return null
    }
}

@Singleton(binds = [AccountSignupDataSource::class])
internal class AccountApiSignupDataSource(
    private val api: AndroidAccountAPI,
) : AccountSignupDataSource {
    override suspend fun createAccount(
        purchase: PurchaseData,
        obfuscatedDeviceId: String,
    ): AccountCreation {
        val information =
            AndroidVpnSignupInformation(
                store = RECEIPT_STORE,
                receipt = AndroidVpnSignupInformation.Receipt(purchase.orderId, purchase.token, purchase.productId),
                obfuscatedDeviceId = obfuscatedDeviceId,
            )
        return suspendCancellableCoroutine { continuation ->
            api.vpnSignUp(information) { details, errors ->
                continuation.resume(
                    when {
                        details != null -> AccountCreation.Created(Credentials(details.status, details.username, details.password))
                        errors.isNetworkError() -> AccountCreation.NetworkError(errors.first().message ?: "Network error")
                        else -> AccountCreation.Rejected
                    },
                )
            }
        }
    }

    override suspend fun setEmail(email: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            api.setEmail(email, false) { _, errors -> continuation.resume(errors.isEmpty()) }
        }

    private fun List<AccountRequestError>.isNetworkError() = any { it.code == NETWORK_ERROR_CODE }
}

@Singleton(binds = [ObfuscatedDeviceIdentifier::class])
internal class AndroidObfuscatedDeviceIdentifier(
    private val context: Context,
) : ObfuscatedDeviceIdentifier {
    override fun invoke(): String? =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)?.let { androidId ->
            val hash = MessageDigest.getInstance("SHA-512").digest(androidId.toByteArray(Charsets.UTF_8))
            Base64.getEncoder().encodeToString(hash)
        }
}