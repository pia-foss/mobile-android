plugins {
    alias(libs.plugins.pia.android.feature)
}

android {
    namespace = "com.kape.splash"
}

dependencies {
    implementation(project(":core:regions"))
    implementation(project(":core:account"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:utils"))
    implementation(project(":capabilities:featureflags"))
    implementation(project(":capabilities:buildconfig"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}