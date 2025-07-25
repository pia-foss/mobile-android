import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {

    private const val IMPLEMENTATION = "implementation"
    private const val TEST_IMPLEMENTATION = "testImplementation"
    private const val TEST_RUNTIME_ONLY = "testRuntimeOnly"
    private const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    private const val JETPACK = "2.9.1"
    private const val KOIN = "4.1.0"
    const val KOTLIN_COMPILER_EXTENSION = "1.5.15"

    fun DependencyHandler.implementFeatureModule() {
    }
}