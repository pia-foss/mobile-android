package com.kape.splash.data

import com.kape.splash.domain.AppRelease
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.LatestClientVersion
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

internal class AccountAppUpdateRepositoryTest {
    private val api = FakeAccountApi()
    private val repository = AccountAppUpdateRepository(api)

    @Test
    fun `maps the latest release`() =
        runTest {
            api.release = release(versionCode = "412", url = "https://example.com/pia.apk")

            assertEquals(AppRelease("https://example.com/pia.apk", 412), repository.getLatestRelease())
        }

    @Test
    fun `there is no release when the request fails`() =
        runTest {
            api.release = release(versionCode = "412", url = "https://example.com/pia.apk")
            api.errors = listOf(AccountRequestError(500, "error"))

            assertNull(repository.getLatestRelease())
        }

    @Test
    fun `there is no release without a response`() =
        runTest {
            assertNull(repository.getLatestRelease())
        }

    @Test
    fun `a release without a download url is ignored`() =
        runTest {
            api.release = release(versionCode = "412", url = "")

            assertNull(repository.getLatestRelease())
        }

    @Test
    fun `a release with a non-numeric version code is ignored`() =
        runTest {
            api.release = release(versionCode = "4.1.2", url = "https://example.com/pia.apk")

            assertNull(repository.getLatestRelease())
        }

    private fun release(
        versionCode: String,
        url: String,
    ) = LatestClientVersion(versionName = "4.1.2", versionCode = versionCode, sha256 = "sha", url = url)
}

private class FakeAccountApi : AndroidAccountAPI by unsupported() {
    var release: LatestClientVersion? = null
    var errors = emptyList<AccountRequestError>()

    override fun latestRelease(callback: (LatestClientVersion?, List<AccountRequestError>) -> Unit) {
        callback(release, errors)
    }
}

private fun unsupported(): AndroidAccountAPI =
    Proxy.newProxyInstance(
        AndroidAccountAPI::class.java.classLoader,
        arrayOf(AndroidAccountAPI::class.java),
    ) { _, method, _ -> throw UnsupportedOperationException(method.name) } as AndroidAccountAPI