import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.pia.android.library)
    alias(libs.plugins.pia.android.flavors)
}

configure<LibraryExtension> {
    namespace = "com.kape.location"

    shareFlavorSources("playservices", ProviderFlavor.WITH_PLAY_SERVICES)
    shareFlavorSources("noplayservices", listOf(ProviderFlavor.AMAZON, ProviderFlavor.META, ProviderFlavor.FDROID))
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":capabilities:ui"))
    implementation(libs.coroutines.android)
    implementation(libs.androidx.core.ktx)
    ProviderFlavor.WITH_PLAY_SERVICES.forEach { add("${it}Implementation", libs.play.services.location) }

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
}