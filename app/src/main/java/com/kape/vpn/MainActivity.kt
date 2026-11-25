package com.kape.vpn

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kape.contracts.Router
import com.kape.data.Splash
import com.kape.router.navigateBack
import com.kape.ui.theme.PIATheme
import com.kape.ui.theme.PiaScreen
import com.kape.utils.PlatformUtils
import com.kape.vpn.main.MainViewModel
import com.kape.vpn.main.appEntryProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CROSS_FADE_DURATION_MS = 700

// AppCompatActivity, so AppCompatDelegate's night mode, which PIATheme sets on TV, applies to it.
class MainActivity : AppCompatActivity() {
    private val router: Router by inject()
    private val platformUtils: PlatformUtils by inject()
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTv = platformUtils.isTv()
        if (!BuildConfig.DEBUG) {
            // Keeps account and connection details out of screenshots and the recent apps screen.
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        if (isTv) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // Only on a fresh start, so a rotation doesn't handle the same intent again.
        if (savedInstanceState == null) viewModel.onIntent(intent.action, intent.dataString)

        enableEdgeToEdge()
        setContent {
            PIATheme(isTv) {
                PiaScreen(isTv = isTv, router = router, startDestination = Splash) { backStack ->
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.navigateBack() },
                            entryDecorators =
                                listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                ),
                            transitionSpec = { crossFade() },
                            popTransitionSpec = { crossFade() },
                            predictivePopTransitionSpec = { crossFade() },
                            entryProvider = remember(isTv) { appEntryProvider(isTv) },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onIntent(intent.action, intent.dataString)
    }

    private fun crossFade(): ContentTransform {
        val animationSpec = tween<Float>(CROSS_FADE_DURATION_MS)
        return fadeIn(animationSpec) togetherWith fadeOut(animationSpec)
    }
}