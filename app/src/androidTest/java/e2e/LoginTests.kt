package e2e

import androidx.test.uiautomator.uiAutomator
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
                TestCredentials.username
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
                TestCredentials.password
            onElement { viewIdResourceName == Login.LOGIN_BUTTON }.click()
            assertNotNull(onElementOrNull { viewIdResourceName == Login.ERROR_FIELD })
        }
}