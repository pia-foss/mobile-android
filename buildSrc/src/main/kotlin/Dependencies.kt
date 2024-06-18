import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    private const val IMPLEMENTATION = "implementation"
    private const val TEST_IMPLEMENTATION = "testImplementation"
    private const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    private const val DEBUG_IMPLEMENTATION = "debugImplementation"
    private const val ANDROID_TEST_UTIL = "androidTestUtil"
    private const val GOOGLE_IMPLEMENTATION = "googleImplementation"
    private const val AMAZON_IMPLEMENTATION = "amazonImplementation"
    private const val COMPOSE_BOM = "androidx.compose:compose-bom:2024.04.00"
    private const val COMPOSE_TV = "1.0.0-alpha10"
    private const val COMPOSE_NAVIGATION = "2.7.7"
    private const val JETPACK = "2.7.0"
    private const val ACCOMPANIST = "0.34.0"
    private const val KOIN = "3.5.6"
    const val KOTLIN_COMPILER_EXTENSION = "1.5.11"

    const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:2.0.4"

    fun DependencyHandler.implementCompose() {
        add(IMPLEMENTATION, platform(COMPOSE_BOM))
        add(IMPLEMENTATION, "androidx.compose.ui:ui")
        add(IMPLEMENTATION, "androidx.compose.ui:ui-graphics")
        add(IMPLEMENTATION, "androidx.compose.ui:ui-tooling-preview")
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-systemuicontroller:$ACCOMPANIST")
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
        add(IMPLEMENTATION, "androidx.core:core-ktx:1.12.0")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-runtime-ktx:$JETPACK")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-runtime-compose:$JETPACK")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-viewmodel-ktx:$JETPACK")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-process:$JETPACK")
        add(IMPLEMENTATION, "androidx.activity:activity-compose:1.8.2")
        add(IMPLEMENTATION, "androidx.appcompat:appcompat:1.6.1")
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
        add(IMPLEMENTATION, "com.russhwolf:multiplatform-settings:0.8")
    }

    fun DependencyHandler.implementSerialization() {
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    }

    fun DependencyHandler.implementKtor() {
        add(IMPLEMENTATION, "io.ktor:ktor-client-okhttp:2.3.10")
        add(IMPLEMENTATION, "io.ktor:ktor-client-core:2.3.10")
    }

    fun DependencyHandler.implementPayments() {
        add(GOOGLE_IMPLEMENTATION, "com.android.billingclient:billing-ktx:6.2.1")
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
        add(TEST_IMPLEMENTATION, "io.mockk:mockk-android:1.13.10")
        add(TEST_IMPLEMENTATION, "app.cash.turbine:turbine:1.1.0")
        add(TEST_IMPLEMENTATION, "org.junit.jupiter:junit-jupiter-params:5.10.1")
    }

    fun DependencyHandler.implementAndroidTest() {
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test.ext:junit:1.1.5")
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test.espresso:espresso-core:3.5.1")
        add(ANDROID_TEST_IMPLEMENTATION, "io.mockk:mockk-android:1.13.5")
    }

    fun DependencyHandler.implementAndroidUiTest() {
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test.uiautomator:uiautomator:2.2.0")
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test:runner:1.5.2")
        add(ANDROID_TEST_UTIL, "androidx.test:orchestrator:1.4.2")
    }

    fun DependencyHandler.implementCoroutines() {
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
        add(TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
        add(ANDROID_TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
        add(ANDROID_TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    }

    fun DependencyHandler.implementAccount() {
        add(IMPLEMENTATION, "com.kape.android:account-android:1.5.2")
    }

    fun DependencyHandler.implementKpi() {
        add(IMPLEMENTATION, "com.kape.android:kpi-android:1.2.2")
    }

    fun DependencyHandler.implementCsi() {
        add(IMPLEMENTATION, "com.kape.android:csi-android:1.3.2")
    }

    fun DependencyHandler.implementRegions() {
        add(IMPLEMENTATION, "com.kape.android:regions-android:1.7.0")
    }

    fun DependencyHandler.implementObfuscator() {
        add(IMPLEMENTATION, "com.kape.android:obfuscator:0.0.2")
    }

    fun DependencyHandler.implementVpnManager() {
        add(IMPLEMENTATION, "com.kape.android:vpnmanager:0.3.7")
    }

    fun DependencyHandler.implementDrawablePainter() {
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-drawablepainter:$ACCOMPANIST")
    }

    fun DependencyHandler.implementGlance() {
        add(IMPLEMENTATION, "androidx.glance:glance-appwidget:1.0.0")
        add(IMPLEMENTATION, "androidx.glance:glance-material3:1.0.0")
        implementMaterial3()
    }

    fun DependencyHandler.implementReorderable() {
        add(IMPLEMENTATION, "org.burnoutcrew.composereorderable:reorderable:0.9.6")
    }

    fun DependencyHandler.implementWebView() {
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-webview:$ACCOMPANIST")
    }

    fun DependencyHandler.implementConstraintLayout() {
        add(IMPLEMENTATION, "androidx.constraintlayout:constraintlayout-compose:1.0.1")
    }

    private fun DependencyHandler.implementMaterial3() {
        add(IMPLEMENTATION, "androidx.compose.material3:material3:1.2.1")
    }

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }
}