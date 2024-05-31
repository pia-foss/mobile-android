import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    // TODO: The Dependencies module is built after this step. Using it causes Unresolved
    // TODO: reference errors. Do/Can we move to a later stage in the build process?
    implementation("com.google.code.gson:gson:2.10.1")
    gradleApi()
}

gradlePlugin {
    plugins {
        create("kape.licenses") {
            id = "kape.licenses"
            implementationClass = "LicensePlugin"
        }
    }
}