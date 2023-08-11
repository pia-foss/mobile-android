import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    internal const val IMPLEMENTATION = "implementation"
    internal const val TEST_IMPLEMENTATION = "testImplementation"
    internal const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    internal const val DEBUG_IMPLEMENTATION = "debugImplementation"
    internal const val GOOGLE_IMPLEMENTATION = "googleImplementation"
    internal const val AMAZON_IMPLEMENTATION = "amazonImplementation"
    internal const val COMPOSE_BOM = "androidx.compose:compose-bom:2023.03.00"
    internal const val COMPOSE_NAVIGATION = "2.6.0"

    const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:2.0.3"

    fun DependencyHandler.implementCompose() {
        add(IMPLEMENTATION, platform(COMPOSE_BOM))
        add(IMPLEMENTATION, "androidx.compose.ui:ui")
        add(IMPLEMENTATION, "androidx.compose.ui:ui-graphics")
        add(IMPLEMENTATION, "androidx.compose.ui:ui-tooling-preview")
        add(IMPLEMENTATION, "androidx.compose.material3:material3")
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-systemuicontroller:0.30.1")
        add(ANDROID_TEST_IMPLEMENTATION, platform(COMPOSE_BOM))
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.compose.ui:ui-test-junit4")
        add(DEBUG_IMPLEMENTATION, "androidx.compose.ui:ui-tooling")
        add(DEBUG_IMPLEMENTATION, "androidx.compose.ui:ui-test-manifest")
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
        add(IMPLEMENTATION, "com.google.accompanist:accompanist-swiperefresh:0.30.1")
    }

    fun DependencyHandler.implementViewModel() {
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    }

    fun DependencyHandler.implementAndroidBase() {
        add(IMPLEMENTATION, "androidx.core:core-ktx:1.10.1")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
        add(IMPLEMENTATION, "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
        add(IMPLEMENTATION, "androidx.activity:activity-compose:1.7.2")
    }

    fun DependencyHandler.implementKoin() {
        add(IMPLEMENTATION, "io.insert-koin:koin-core:3.4.3")
        add(IMPLEMENTATION, "io.insert-koin:koin-android:3.4.3")
        add(IMPLEMENTATION, "io.insert-koin:koin-androidx-compose:3.4.6")
        add(IMPLEMENTATION, "io.insert-koin:koin-androidx-navigation:3.4.3")
        add(TEST_IMPLEMENTATION, "io.insert-koin:koin-test-junit5:3.4.1")
        add(TEST_IMPLEMENTATION, "io.insert-koin:koin-android-test:3.4.3")
        add(ANDROID_TEST_IMPLEMENTATION, "io.insert-koin:koin-android-test:3.4.3")
        add(ANDROID_TEST_IMPLEMENTATION, "io.insert-koin:koin-test:3.4.3")
    }

    fun DependencyHandler.implementCrypto() {
        add(IMPLEMENTATION, "androidx.security:security-crypto-ktx:1.1.0-alpha06")
    }

    fun DependencyHandler.implementMultiplatformSettings() {
        add(IMPLEMENTATION, "com.russhwolf:multiplatform-settings:0.8")
    }

    fun DependencyHandler.implementSerialization() {
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    }

    fun DependencyHandler.implementPayments() {
        add(GOOGLE_IMPLEMENTATION, "com.android.billingclient:billing-ktx:5.0.0")
        add(AMAZON_IMPLEMENTATION, "com.amazon.device:amazon-appstore-sdk:3.0.2")
    }

    fun DependencyHandler.implementKtor() {
        add(IMPLEMENTATION, "io.ktor:ktor-client-okhttp:1.6.5")
        add(IMPLEMENTATION, "io.ktor:ktor-client-core:1.6.5")
    }

    fun DependencyHandler.implementFeatureModule() {
        implementCompose()
        implementComposeNavigation()
        implementViewModel()
        implementKoin()
        implementKtor()
        implementCoroutines()

        implementTest()
    }

    fun DependencyHandler.implementTest() {
        add(TEST_IMPLEMENTATION, "junit:junit:4.13.2")
        add(TEST_IMPLEMENTATION, "io.mockk:mockk-android:1.13.5")
        add(TEST_IMPLEMENTATION, "app.cash.turbine:turbine:1.0.0")
        add(TEST_IMPLEMENTATION, "org.junit.jupiter:junit-jupiter-params:5.10.0")
    }

    fun DependencyHandler.implementAndroidTest() {
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test.ext:junit:1.1.5")
        add(ANDROID_TEST_IMPLEMENTATION, "androidx.test.espresso:espresso-core:3.5.1")
        add(ANDROID_TEST_IMPLEMENTATION, "io.mockk:mockk-android:1.13.5")
    }

    fun DependencyHandler.implementCoroutines() {
        add(IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        add(TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        add(ANDROID_TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        add(ANDROID_TEST_IMPLEMENTATION, "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    }
}
