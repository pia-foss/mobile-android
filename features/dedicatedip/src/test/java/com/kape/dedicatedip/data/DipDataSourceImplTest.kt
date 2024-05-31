package com.kape.dedicatedip.data

import app.cash.turbine.test
import com.kape.dedicatedip.di.dedicatedIpModule
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.dip.DipPrefs
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.stream.Stream

class DipDataSourceImplTest {

    private val api: AndroidAccountAPI = mockk(relaxed = true)
    private val prefs: DipPrefs = mockk()

    private lateinit var source: DipDataSource

    private val appModule = module {
        single { api }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, dedicatedIpModule(appModule))
        }
        source = DipDataSourceImpl(api, prefs)
    }

    @Test
    fun activateSuccess() {
        runTest {
            val dipInfo = DedicatedIPInformationResponse.DedicatedIPInformation(
                id = "id",
                ip = "ip",
                cn = "cn",
                groups = null,
                dip_expire = null,
                dipToken = "ipToken",
                status = DedicatedIPInformationResponse.Status.active,
            )
            every { prefs.addDedicatedIp(any()) } returns Unit
            coEvery { api.dedicatedIPs(any(), any()) } answers {
                lastArg<(List<DedicatedIPInformationResponse.DedicatedIPInformation>, List<AccountRequestError>) -> Unit>().invoke(
                    listOf(dipInfo),
                    emptyList(),
                )
            }

            source.activate("ipToken").test {
                val actual = awaitItem()
                assertEquals(DipApiResult.Active, actual)
            }
        }
    }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun activateFail(errorList: List<AccountRequestError>, expected: DipApiResult) =
        runTest {
            coEvery { api.dedicatedIPs(any(), any()) } answers {
                lastArg<(List<DedicatedIPInformationResponse.DedicatedIPInformation>, List<AccountRequestError>) -> Unit>().invoke(
                    emptyList(),
                    errorList,
                )
            }

            source.activate("ipToken").test {
                val actual = awaitItem()
                assertEquals(expected, actual)
            }
        }

    @ParameterizedTest(name = "api: {0}, expected: {1}")
    @MethodSource("accountApiResults")
    fun renew(errorList: List<AccountRequestError>, expected: DipApiResult) =
        runTest {
            coEvery { api.renewDedicatedIP(any(), any()) } answers {
                lastArg<(List<AccountRequestError>) -> Unit>().invoke(errorList)
            }

            source.renew("ipToken").test {
                val actual = awaitItem()
                kotlin.test.assertEquals(expected, actual)
            }
        }

    companion object {

        @JvmStatic
        fun accountApiResults() = Stream.of(
            Arguments.of(
                listOf(AccountRequestError(code = 600, message = null)),
                DipApiResult.Error,
            ),
            Arguments.of(
                listOf(AccountRequestError(code = 429, message = null)),
                DipApiResult.Error,
            ),
            Arguments.of(
                listOf(AccountRequestError(code = 401, message = null)),
                DipApiResult.Error,
            ),
            Arguments.of(
                listOf(AccountRequestError(code = 402, message = null)),
                DipApiResult.Error,
            ),
        )
    }
}