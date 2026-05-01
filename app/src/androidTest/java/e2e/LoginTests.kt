package e2e

import androidx.test.uiautomator.uiAutomator
import com.kape.vpn.test.BuildConfig
import org.junit.Test
import kotlin.test.assertNotNull

class LoginTests {
    @Test
    fun loginWithValidCredentialsReachesConnectionScreen() =
        uiAutomator {
            login()
            assertNotNull(onElementOrNull { viewIdResourceName == Main.CONNECT_BUTTON })
        }

    @Test
    fun loginWithNoCredentialsShowsError() =
        uiAutomator {
            reachLogin()
            onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
            assertNotNull(onElementOrNull { viewIdResourceName == Login.ERROR_FIELD })
        }

    @Test
    fun loginWithInvalidCredentialsShowsError() =
        uiAutomator {
            reachLogin()
            onElement { viewIdResourceName == Login.USERNAME_FIELD }.text = INVALID
            onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text = INVALID
            onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
            assertNotNull(onElementOrNull { viewIdResourceName == Login.ERROR_FIELD })
        }

    @Test
    fun loginWithValidUsernameAndInvalidPasswordShowsError() =
        uiAutomator {
            reachLogin()
            onElement { viewIdResourceName == Login.USERNAME_FIELD }.text =
                BuildConfig.PIA_VALID_USERNAME
            onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text = INVALID
            onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
            assertNotNull(onElementOrNull { viewIdResourceName == Login.ERROR_FIELD })
        }

    @Test
    fun loginWithInvalidUsernameAndValidPasswordShowsError() =
        uiAutomator {
            reachLogin()
            onElement { viewIdResourceName == Login.USERNAME_FIELD }.text = INVALID
            onElement { viewIdResourceName == Login.PASSWORD_FIELD }.text =
                BuildConfig.PIA_VALID_PASSWORD
            onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
            assertNotNull(onElementOrNull { viewIdResourceName == Login.ERROR_FIELD })
        }
}