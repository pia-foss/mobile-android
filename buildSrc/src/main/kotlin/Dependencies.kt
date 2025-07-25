import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    private const val IMPLEMENTATION = "implementation"
    private const val TEST_IMPLEMENTATION = "testImplementation"
    private const val TEST_RUNTIME_ONLY = "testRuntimeOnly"
    private const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    private const val DEBUG_IMPLEMENTATION = "debugImplementation"
    private const val ANDROID_TEST_UTIL = "androidTestUtil"
    private const val GOOGLE_IMPLEMENTATION = "googleImplementation"
    private const val AMAZON_IMPLEMENTATION = "amazonImplementation"
    private const val COMPOSE_BOM = "androidx.compose:compose-bom:2025.06.01"
    private const val COMPOSE_TV = "1.0.0-alpha10"
    private const val COMPOSE_NAVIGATION = "2.9.0"
    private const val JETPACK = "2.9.1"
    private const val ACCOMPANIST = "0.36.0"
    private const val KOIN = "4.1.0"
    const val KOTLIN_COMPILER_EXTENSION = "1.5.15"

    const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:2.1.5"

    fun DependencyHandler.implementCompose() {
        add(IMPLEMENTATION, platform(COMPOSE_BOM))
        add(IMPLEMENTATION, "androidx.compose.ui:ui")
        add(IMPLEMENTATION, "androidx.compose.ui:ui-graphics")
        add(IMPLEMENTATION, "androidx.compose.ui:ui-tooling-preview")
        add(IMPLEMENTATION, "androidx.compose.foundation:foundation")
        add(IMPLEMENTATION, "androidx.compose.runtime:runtime")
        add(ANDROID_TEST_IMPLEMENTATION, platform(COMPOSE_BOM))
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.compose.ui:ui-test-junit4")
        add(DEBUG_IMPLEMENTATION, "androidx.compose.ui:ui-tooling")
        add(DEBUG_IMPLEMENTATION, "androidx.compose.ui:ui-test-manifest")
        implementMaterial3()
    }

    fun DependencyHandler.implementComposeTv() {
        add(IMPLEMENTATION, "androidx.tv:tv-foundation:$COMPOSE_TV")
        add(IMPLEMENTATION, "androidx.tv:tv-material:$COMPOSE_TV")
    }

    fun DependencyHandler.implementComposeNavigation() {
        add(IMPLEMENTATION, "androidx.navigation:navigation-compose:$COMPOSE_NAVIGATION")
        add(
            ANDROID_TEST_IMPLEMENTATION,
            "androidx.navigation:navigation-testing:$COMPOSE_NAVIGATION",
        )
    }

    fun DependencyHandler.implementSwipeToRefresh() {
        add(IMPLEMENTATION, "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-swiperefresh:$ACCOMPANIST")
    }

    fun DependencyHandler.implementViewModel() {
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-viewmodel-ktx:$JETPACK")
    }

    fun DependencyHandler.implementAndroidBase() {
        add(IMPLEMENTATION, "androidx.core:core-ktx:1.16.0")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-runtime-ktx:$JETPACK")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-runtime-compose:$JETPACK")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-viewmodel-ktx:$JETPACK")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-process:$JETPACK")
        add(IMPLEMENTATION, "androidx.activity:activity-compose:1.10.1")
        add(IMPLEMENTATION, "androidx.appcompat:appcompat:1.7.1")
    }

    fun DependencyHandler.implementKoin() {
        add(IMPLEMENTATION, "io.insert-koin:koin-core:$KOIN")
        add(IMPLEMENTATION, "io.insert-koin:koin-android:$KOIN")
        add(IMPLEMENTATION, "io.insert-koin:koin-androidx-compose:$KOIN")
        add(IMPLEMENTATION, "io.insert-koin:koin-androidx-navigation:$KOIN")
        add(TEST_IMPLEMENTATION, "io.insert-koin:koin-test-junit5:$KOIN")
        add(TEST_IMPLEMENTATION, "io.insert-koin:koin-android-test:$KOIN")
        add(ANDROID_TEST_IMPLEMENTATION, "io.insert-koin:koin-android-test:$KOIN")
        add(ANDROID_TEST_IMPLEMENTATION, "io.insert-koin:koin-test:$KOIN")
    }

    fun DependencyHandler.implementCrypto() {
        add(IMPLEMENTATION, "androidx.security:security-crypto-ktx:1.1.0-alpha06")
    }

    fun DependencyHandler.implementMultiplatformSettings() {
        add(IMPLEMENTATION, "com.russhwolf:multiplatform-settings:1.3.0")
    }

    fun DependencyHandler.implementSerialization() {
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    }

    fun DependencyHandler.implementKtor() {
        add(IMPLEMENTATION, "io.ktor:ktor-client-okhttp:3.2.1")
        add(IMPLEMENTATION, "io.ktor:ktor-client-core:3.2.1")
    }

    fun DependencyHandler.implementPayments() {
        add(GOOGLE_IMPLEMENTATION, "com.android.billingclient:billing-ktx:7.1.1")
        add(AMAZON_IMPLEMENTATION, "com.amazon.device:amazon-appstore-sdk:3.0.4")
    }

    fun DependencyHandler.implementSpongyCastle() {
        add(IMPLEMENTATION, "com.madgag.spongycastle:core:1.58.0.0")
    }

    fun DependencyHandler.implementFeatureModule() {
        implementCompose()
        implementComposeTv()
        implementComposeNavigation()
        implementViewModel()
        implementKoin()
        implementCoroutines()
        implementAndroidBase()
        implementTest()
    }

    fun DependencyHandler.implementTest() {
        add(TEST_IMPLEMENTATION, "junit:junit:4.13.2")
        add(TEST_IMPLEMENTATION, "io.mockk:mockk-android:1.14.4")
        add(TEST_IMPLEMENTATION, "app.cash.turbine:turbine:1.2.1")
        add(TEST_IMPLEMENTATION, "org.junit.jupiter:junit-jupiter-params:5.12.2")
        add(TEST_RUNTIME_ONLY, "org.junit.platform:junit-platform-launcher:1.12.2")
    }

    fun DependencyHandler.implementAndroidUiTest() {
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test.uiautomator:uiautomator:2.2.0")
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test:runner:1.5.2")
        add(ANDROID_TEST_UTIL, "androidx.test:orchestrator:1.4.1")
    }

    fun DependencyHandler.implementCoroutines() {
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
        add(TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        add(ANDROID_TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
        add(ANDROID_TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    }

    fun DependencyHandler.implementAccount() {
        add(IMPLEMENTATION, "com.kape.android:account-android:1.5.3")
    }

    fun DependencyHandler.implementKpi() {
        add(IMPLEMENTATION, "com.kape.android:kpi-android:1.2.4")
    }

    fun DependencyHandler.implementCsi() {
        add(IMPLEMENTATION, "com.kape.android:csi-android:1.3.4")
    }

    fun DependencyHandler.implementRegions() {
        add(IMPLEMENTATION, "com.kape.android:regions-android:1.7.1")
    }

    fun DependencyHandler.implementObfuscator() {
        add(IMPLEMENTATION, "com.kape.android:obfuscator:0.0.2")
    }

    fun DependencyHandler.implementVpnManager() {
        add(IMPLEMENTATION, "com.kape.android:vpnmanager:0.4.1-pia")
    }

    fun DependencyHandler.implementDrawablePainter() {
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-drawablepainter:0.37.2")
    }

    fun DependencyHandler.implementGlance() {
        add(IMPLEMENTATION, "androidx.glance:glance-appwidget:1.1.1")
        add(IMPLEMENTATION, "androidx.glance:glance-material3:1.1.1")
        implementMaterial3()
    }

    fun DependencyHandler.implementReorderable() {
        add(IMPLEMENTATION, "sh.calvin.reorderable:reorderable:2.5.1")
    }

    fun DependencyHandler.implementWebView() {
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-webview:$ACCOMPANIST")
    }

    fun DependencyHandler.implementConstraintLayout() {
        add(IMPLEMENTATION, "androidx.constraintlayout:constraintlayout-compose:1.1.1")
    }

    private fun DependencyHandler.implementMaterial3() {
        add(IMPLEMENTATION, "androidx.compose.material3:material3:1.3.2")
    }

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }
}