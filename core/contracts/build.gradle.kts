plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.contracts"
}

dependencies {
    api(project(":core:data"))
}