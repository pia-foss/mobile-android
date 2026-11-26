import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.pia.android.feature)
    alias(libs.plugins.pia.android.flavors)
}

configure<LibraryExtension> {
    namespace = "com.kape.dedicatedip"

    shareFlavorSources(
        directory = "nostore",
        flavors = listOf(ProviderFlavor.AMAZON, ProviderFlavor.NO_IN_APP, ProviderFlavor.META, ProviderFlavor.FDROID),
    )
}

dependencies {
    implementation(project(":core:account"))
    implementation(project(":core:regions"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":capabilities:appbar"))
    implementation(libs.androidx.constraintlayout.compose)
    "googleImplementation"(project(":core:payments"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}