object Android {
    const val androidCore = "androidx.core:core-ktx:1.7.0"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:2.4.1"
    const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1"
    const val security = "androidx.security:security-crypto:1.0.0"
    const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:1.1.5"

    const val cryptoEddsa = "net.i2p.crypto:eddsa:0.3.0"
    const val spongyCastle = "com.madgag.spongycastle:core:1.54.0.0"
}

object Compose {
    const val COMPOSE_VERSION: String = "1.1.1"

    const val ui = "androidx.compose.ui:ui:$COMPOSE_VERSION"
    const val material = "androidx.compose.material:material:$COMPOSE_VERSION"
    const val preview = "androidx.compose.ui:ui-tooling-preview:$COMPOSE_VERSION"
    const val activity = "androidx.activity:activity-compose:1.4.0"
    const val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1"
    const val navigation = "androidx.navigation:navigation-compose:2.4.2"
}

object Koin {
    const val core = "io.insert-koin:koin-core:3.1.6"
    const val android = "io.insert-koin:koin-android:3.1.6"
    const val compose = "io.insert-koin:koin-androidx-compose:3.1.6"
    const val navigation = "io.insert-koin:koin-androidx-navigation:3.1.6"
}

object Kotlin {
    const val KTOR_VERSION = "1.6.5"
    const val COROUTINES_VERSION = "1.6.0-native-mt"

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib"
    const val kotlinStdLibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common"
    const val ktor = "io.ktor:ktor-client-okhttp:$KTOR_VERSION"
    const val ktorClientCore = "io.ktor:ktor-client-core:$KTOR_VERSION"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0"
    const val serializationCore = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2"
    const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
    const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.2"
}

object UnitTest {
    const val junit = "junit:junit:4.13.2"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0"
    const val turbine = "app.cash.turbine:turbine:0.8.0"
    const val koinTestJunit5 = "io.insert-koin:koin-test-junit5:3.1.6"
    const val koinTest = "io.insert-koin:koin-test:3.1.6"
    const val jUnit5Api = "org.junit.jupiter:junit-jupiter-api:5.8.2"
    const val jUnit5Engine = "org.junit.jupiter:junit-jupiter-engine:5.8.2"
    const val jUnit5Params = "org.junit.jupiter:junit-jupiter-params:5.8.2"
    const val mockk = "io.mockk:mockk:1.12.3"
    const val androidCoreArch = "androidx.arch.core:core-testing:2.1.0"
}

object AndroidTest {
    const val jUnit = "androidx.test.ext:junit:1.1.3"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
    const val composeUI = "androidx.compose.ui:ui-test-junit4:${Compose.COMPOSE_VERSION}"
    const val mockk = "io.mockk:mockk-android:1.12.3"
    const val koinTest = "io.insert-koin:koin-test:3.1.6"
    const val androidCore = "androidx.test:core-ktx:1.4.0"
    const val uiAutomator = "androidx.test.uiautomator:uiautomator:2.2.0"

    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Kotlin.COROUTINES_VERSION}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Kotlin.COROUTINES_VERSION}"
}

object DebugTest {
    const val composeUI = "androidx.compose.ui:ui-tooling:${Compose.COMPOSE_VERSION}"
    const val composeManifest = "androidx.compose.ui:ui-test-manifest:${Compose.COMPOSE_VERSION}"
}
