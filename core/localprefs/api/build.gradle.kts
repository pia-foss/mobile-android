plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.localprefs.api"
}

dependencies {
    api(project(":core:localprefs:data"))
    api(project(":core:data"))
    api(libs.coroutines.android)
}